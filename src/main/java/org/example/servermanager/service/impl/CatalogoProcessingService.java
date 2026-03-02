package org.example.servermanager.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.servermanager.entity.ArchivoCatalogo;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.enums.TipoFuenteCatalogo;
import org.example.servermanager.repository.ArchivoCatalogoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CatalogoProcessingService {

    private final ArchivoCatalogoRepository catalogoRepository;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public CatalogoProcessingService(ArchivoCatalogoRepository catalogoRepository) {
        this.catalogoRepository = catalogoRepository;
    }

    /**
     * Procesa un archivo local (PDF o Excel) subido desde el dashboard
     */
    public ArchivoCatalogo procesarArchivoLocal(MultipartFile file, Empresa empresa) throws IOException {
        String nombreOriginal = file.getOriginalFilename();
        String extension = getExtension(nombreOriginal).toLowerCase();

        String textoExtraido;
        int totalPaginas = 0;
        String rutaPaginas = null;

        if ("pdf".equals(extension)) {
            try (InputStream is = file.getInputStream()) {
                textoExtraido = extraerTextoPdf(is);
            }
            // Convertir páginas a imágenes
            rutaPaginas = generarRutaPaginas(empresa.getId());
            try (InputStream is = file.getInputStream()) {
                totalPaginas = convertirPaginasAPng(is, rutaPaginas);
            }
        } else if ("xlsx".equals(extension) || "xls".equals(extension)) {
            try (InputStream is = file.getInputStream()) {
                textoExtraido = extraerTextoExcel(is);
            }
        } else {
            throw new IllegalArgumentException("Formato no soportado: " + extension + ". Use PDF o Excel (.xlsx)");
        }

        ArchivoCatalogo catalogo = ArchivoCatalogo.builder()
                .empresa(empresa)
                .nombreOriginal(nombreOriginal)
                .tipoFuente(TipoFuenteCatalogo.ARCHIVO)
                .textoExtraido(textoExtraido)
                .totalPaginas(totalPaginas)
                .rutaPaginas(rutaPaginas)
                .activo(true)
                .build();

        catalogo = catalogoRepository.save(catalogo);
        log.info("Catálogo procesado: {} ({} páginas, {} chars de texto)",
                nombreOriginal, totalPaginas, textoExtraido.length());
        return catalogo;
    }

    /**
     * Procesa un link de Google Drive (PDF) o Google Sheets
     */
    public ArchivoCatalogo procesarLink(String url, Empresa empresa) throws IOException {
        if (url.contains("docs.google.com/spreadsheets")) {
            return procesarGoogleSheets(url, empresa);
        } else if (url.contains("drive.google.com")) {
            return procesarDrivePdf(url, empresa);
        } else {
            throw new IllegalArgumentException("URL no reconocida. Use un link de Google Drive o Google Sheets.");
        }
    }

    /**
     * Google Sheets: exporta como CSV y parsea
     */
    private ArchivoCatalogo procesarGoogleSheets(String sheetsUrl, Empresa empresa) throws IOException {
        String csvUrl = convertirAUrlExportCsv(sheetsUrl);
        log.info("Descargando Google Sheets como CSV: {}", csvUrl);

        String csvContent = descargarContenido(csvUrl);
        String textoExtraido = parsearCsv(csvContent);

        ArchivoCatalogo catalogo = ArchivoCatalogo.builder()
                .empresa(empresa)
                .nombreOriginal("Google Sheets")
                .tipoFuente(TipoFuenteCatalogo.GOOGLE_SHEETS)
                .urlFuente(sheetsUrl)
                .textoExtraido(textoExtraido)
                .totalPaginas(0)
                .activo(true)
                .build();

        catalogo = catalogoRepository.save(catalogo);
        log.info("Google Sheets procesado: {} chars de texto", textoExtraido.length());
        return catalogo;
    }

    /**
     * Google Drive PDF: descarga temporal, extrae texto e imágenes, borra PDF
     */
    private ArchivoCatalogo procesarDrivePdf(String driveUrl, Empresa empresa) throws IOException {
        String downloadUrl = convertirAUrlDescargaDrive(driveUrl);
        log.info("Descargando PDF de Drive: {}", downloadUrl);

        // Descargar a archivo temporal
        Path tempFile = Files.createTempFile("catalogo_", ".pdf");
        try {
            descargarArchivo(downloadUrl, tempFile);

            String textoExtraido;
            try (InputStream is = Files.newInputStream(tempFile)) {
                textoExtraido = extraerTextoPdf(is);
            }

            String rutaPaginas = generarRutaPaginas(empresa.getId());
            int totalPaginas;
            try (InputStream is = Files.newInputStream(tempFile)) {
                totalPaginas = convertirPaginasAPng(is, rutaPaginas);
            }

            ArchivoCatalogo catalogo = ArchivoCatalogo.builder()
                    .empresa(empresa)
                    .nombreOriginal("PDF de Google Drive")
                    .tipoFuente(TipoFuenteCatalogo.DRIVE_PDF)
                    .urlFuente(driveUrl)
                    .textoExtraido(textoExtraido)
                    .totalPaginas(totalPaginas)
                    .rutaPaginas(rutaPaginas)
                    .activo(true)
                    .build();

            catalogo = catalogoRepository.save(catalogo);
            log.info("Drive PDF procesado: {} páginas, {} chars", totalPaginas, textoExtraido.length());
            return catalogo;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Obtiene el path de la imagen de una página específica
     */
    public Path obtenerImagenPagina(Long catalogoId, int pagina) {
        ArchivoCatalogo catalogo = catalogoRepository.findById(catalogoId)
                .orElseThrow(() -> new IllegalArgumentException("Catálogo no encontrado"));

        if (catalogo.getRutaPaginas() == null) {
            throw new IllegalArgumentException("Este catálogo no tiene imágenes de páginas");
        }

        Path imagePath = Paths.get(catalogo.getRutaPaginas(), "page_" + pagina + ".png");
        if (!Files.exists(imagePath)) {
            throw new IllegalArgumentException("Página " + pagina + " no encontrada");
        }
        return imagePath;
    }

    // ==================== MÉTODOS DE EXTRACCIÓN ====================

    private String extraerTextoPdf(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            StringBuilder resultado = new StringBuilder();
            int totalPages = document.getNumberOfPages();

            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String textoPagina = stripper.getText(document).trim();
                if (!textoPagina.isEmpty()) {
                    resultado.append("[PAGINA ").append(i).append("]\n");
                    resultado.append(textoPagina).append("\n\n");
                }
            }

            String texto = resultado.toString().trim();
            log.info("PDF: {} páginas, {} caracteres extraídos", totalPages, texto.length());
            return texto;
        }
    }

    private int convertirPaginasAPng(InputStream inputStream, String outputDir) throws IOException {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);

        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            for (int i = 0; i < totalPages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 150);
                Path outputFile = dir.resolve("page_" + (i + 1) + ".png");
                ImageIO.write(image, "png", outputFile.toFile());
                log.debug("Página {} convertida a PNG: {}", i + 1, outputFile);
            }

            log.info("Convertidas {} páginas a PNG en {}", totalPages, outputDir);
            return totalPages;
        }
    }

    private String extraerTextoExcel(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            DataFormatter formatter = new DataFormatter();

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (workbook.getNumberOfSheets() > 1) {
                    sb.append("--- Hoja: ").append(sheet.getSheetName()).append(" ---\n");
                }

                for (Row row : sheet) {
                    StringBuilder rowText = new StringBuilder();
                    for (Cell cell : row) {
                        String value = formatter.formatCellValue(cell).trim();
                        if (!value.isEmpty()) {
                            if (rowText.length() > 0) rowText.append(" | ");
                            rowText.append(value);
                        }
                    }
                    if (rowText.length() > 0) {
                        sb.append(rowText).append("\n");
                    }
                }
            }
        }
        String texto = sb.toString().trim();
        log.info("Excel: {} caracteres extraídos", texto.length());
        return texto;
    }

    private String parsearCsv(String csvContent) {
        StringBuilder sb = new StringBuilder();
        String[] lines = csvContent.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                // Reemplazar comas por " | " para legibilidad
                sb.append(trimmed.replace(",", " | ")).append("\n");
            }
        }
        return sb.toString().trim();
    }

    // ==================== UTILIDADES ====================

    private String convertirAUrlExportCsv(String sheetsUrl) {
        // Extraer ID del spreadsheet
        // Formato: https://docs.google.com/spreadsheets/d/{ID}/edit...
        Pattern pattern = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(sheetsUrl);
        if (!matcher.find()) {
            throw new IllegalArgumentException("No se pudo extraer el ID del Google Sheets");
        }
        String id = matcher.group(1);
        return "https://docs.google.com/spreadsheets/d/" + id + "/export?format=csv";
    }

    private String convertirAUrlDescargaDrive(String driveUrl) {
        // Extraer ID del archivo
        // Formato: https://drive.google.com/file/d/{ID}/view...
        Pattern pattern = Pattern.compile("/file/d/([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(driveUrl);
        if (!matcher.find()) {
            throw new IllegalArgumentException("No se pudo extraer el ID del archivo de Drive");
        }
        String id = matcher.group(1);
        return "https://drive.google.com/uc?export=download&id=" + id;
    }

    private String descargarContenido(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setInstanceFollowRedirects(true);
        try (InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes());
        } finally {
            conn.disconnect();
        }
    }

    private void descargarArchivo(String url, Path destino) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setInstanceFollowRedirects(true);
        try (InputStream is = conn.getInputStream()) {
            Files.copy(is, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } finally {
            conn.disconnect();
        }
    }

    private String generarRutaPaginas(Long empresaId) {
        return Paths.get(uploadDir, empresaId.toString(), "catalog-pages").toString();
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot + 1) : "";
    }
}
