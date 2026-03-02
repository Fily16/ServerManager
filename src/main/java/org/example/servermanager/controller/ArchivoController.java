package org.example.servermanager.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.entity.ArchivoCatalogo;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.repository.ArchivoCatalogoRepository;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.service.impl.CatalogoProcessingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/archivos")
@Slf4j
public class ArchivoController {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "pdf", "jpg", "jpeg", "png", "gif", "webp", "doc", "docx", "xls", "xlsx"
    );

    private final CatalogoProcessingService catalogoService;
    private final EmpresaRepository empresaRepository;
    private final ArchivoCatalogoRepository catalogoRepository;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public ArchivoController(CatalogoProcessingService catalogoService,
                             EmpresaRepository empresaRepository,
                             ArchivoCatalogoRepository catalogoRepository) {
        this.catalogoService = catalogoService;
        this.empresaRepository = empresaRepository;
        this.catalogoRepository = catalogoRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> upload(
            @PathVariable Long empresaId,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El archivo esta vacio"));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El archivo excede el tamaño maximo de 50MB"));
        }

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tipo de archivo no permitido. Permitidos: " + String.join(", ", ALLOWED_EXTENSIONS)));
        }

        // Crear directorio si no existe
        Path empresaDir = Paths.get(uploadDir, empresaId.toString());
        Files.createDirectories(empresaDir);

        // Generar nombre unico
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizeFilename(originalName);
        Path filePath = empresaDir.resolve(fileName);

        // Guardar archivo
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Archivo subido: {} para empresa {}", fileName, empresaId);

        // Retornar URL relativa (el frontend construye la URL completa)
        String url = "/api/v1/empresas/" + empresaId + "/archivos/" + fileName;
        Map<String, String> result = Map.of(
                "url", url,
                "nombre", originalName != null ? originalName : fileName,
                "tamaño", formatFileSize(file.getSize())
        );

        return ResponseEntity.ok(ApiResponse.success(result, "Archivo subido exitosamente"));
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<byte[]> getFile(
            @PathVariable Long empresaId,
            @PathVariable String fileName) throws IOException {

        Path filePath = Paths.get(uploadDir, empresaId.toString(), fileName);
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] content = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .body(content);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> listar(@PathVariable Long empresaId) throws IOException {
        Path empresaDir = Paths.get(uploadDir, empresaId.toString());
        if (!Files.exists(empresaDir)) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        try (Stream<Path> paths = Files.list(empresaDir)) {
            List<Map<String, String>> archivos = paths
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        try {
                            return Map.of(
                                    "nombre", p.getFileName().toString(),
                                    "url", "/api/v1/empresas/" + empresaId + "/archivos/" + p.getFileName().toString(),
                                    "tamaño", formatFileSize(Files.size(p))
                            );
                        } catch (IOException e) {
                            return Map.of("nombre", p.getFileName().toString(), "url", "", "tamaño", "");
                        }
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(archivos));
        }
    }

    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long empresaId,
            @PathVariable String fileName) throws IOException {

        Path filePath = Paths.get(uploadDir, empresaId.toString(), fileName);
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Files.delete(filePath);
        log.info("Archivo eliminado: {} de empresa {}", fileName, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Archivo eliminado"));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "archivo";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    // ==================== CATÁLOGO INTELIGENTE ====================

    @PostMapping("/catalogo/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadCatalogo(
            @PathVariable Long empresaId,
            @RequestParam("file") MultipartFile file) {
        try {
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

            ArchivoCatalogo catalogo = catalogoService.procesarArchivoLocal(file, empresa);

            Map<String, Object> result = Map.of(
                    "id", catalogo.getId(),
                    "nombre", catalogo.getNombreOriginal(),
                    "paginas", catalogo.getTotalPaginas(),
                    "caracteres", catalogo.getTextoExtraido().length(),
                    "preview", catalogo.getTextoExtraido().substring(0, Math.min(catalogo.getTextoExtraido().length(), 200))
            );
            return ResponseEntity.ok(ApiResponse.success(result, "Catálogo procesado exitosamente"));
        } catch (Exception e) {
            log.error("Error procesando catálogo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/catalogo/link")
    public ResponseEntity<ApiResponse<Map<String, Object>>> procesarLink(
            @PathVariable Long empresaId,
            @RequestBody Map<String, String> body) {
        try {
            String url = body.get("url");
            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("URL requerida"));
            }

            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

            ArchivoCatalogo catalogo = catalogoService.procesarLink(url, empresa);

            Map<String, Object> result = Map.of(
                    "id", catalogo.getId(),
                    "nombre", catalogo.getNombreOriginal(),
                    "tipo", catalogo.getTipoFuente().name(),
                    "paginas", catalogo.getTotalPaginas(),
                    "caracteres", catalogo.getTextoExtraido().length(),
                    "preview", catalogo.getTextoExtraido().substring(0, Math.min(catalogo.getTextoExtraido().length(), 200))
            );
            return ResponseEntity.ok(ApiResponse.success(result, "Link procesado exitosamente"));
        } catch (Exception e) {
            log.error("Error procesando link: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/catalogo")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarCatalogos(@PathVariable Long empresaId) {
        List<ArchivoCatalogo> catalogos = catalogoRepository.findByEmpresaId(empresaId);

        List<Map<String, Object>> result = catalogos.stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "nombre", c.getNombreOriginal(),
                        "tipo", c.getTipoFuente().name(),
                        "paginas", c.getTotalPaginas(),
                        "caracteres", c.getTextoExtraido() != null ? c.getTextoExtraido().length() : 0,
                        "activo", c.getActivo(),
                        "fecha", c.getFechaCreacion().toString()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/catalogo/{catalogoId}/pagina/{pagina}")
    public ResponseEntity<byte[]> getPaginaCatalogo(
            @PathVariable Long empresaId,
            @PathVariable Long catalogoId,
            @PathVariable int pagina) {
        try {
            Path imagePath = catalogoService.obtenerImagenPagina(catalogoId, pagina);
            byte[] content = Files.readAllBytes(imagePath);
            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/catalogo/{catalogoId}")
    public ResponseEntity<ApiResponse<Void>> eliminarCatalogo(
            @PathVariable Long empresaId,
            @PathVariable Long catalogoId) {

        return catalogoRepository.findById(catalogoId)
                .map(catalogo -> {
                    catalogo.setActivo(false);
                    catalogoRepository.save(catalogo);
                    log.info("Catálogo {} desactivado para empresa {}", catalogoId, empresaId);
                    return ResponseEntity.ok(ApiResponse.<Void>success(null, "Catálogo eliminado"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
