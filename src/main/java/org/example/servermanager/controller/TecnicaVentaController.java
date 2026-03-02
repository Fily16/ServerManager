package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.request.TecnicaVentaRequest;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.TecnicaVentaResponse;
import org.example.servermanager.service.TecnicaVentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestionar las técnicas de venta.
 * Estas técnicas se inyectan en el prompt del bot para que venda como un experto.
 * 
 * Puedes agregar técnicas de libros como:
 * - $100M Offers (Alex Hormozi)
 * - Way of the Wolf (Jordan Belfort)
 * - SPIN Selling
 * - Never Split the Difference (Chris Voss)
 */
@RestController
@RequestMapping("/api/v1/tecnicas-venta")
@RequiredArgsConstructor
@Slf4j
public class TecnicaVentaController {

    private final TecnicaVentaService tecnicaVentaService;

    /**
     * Lista todas las técnicas de venta
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TecnicaVentaResponse>>> listarTodas() {
        List<TecnicaVentaResponse> tecnicas = tecnicaVentaService.listarTodas();
        return ResponseEntity.ok(ApiResponse.success(tecnicas, "Técnicas obtenidas"));
    }

    /**
     * Lista solo las técnicas activas (las que usa el bot)
     */
    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<TecnicaVentaResponse>>> listarActivas() {
        List<TecnicaVentaResponse> tecnicas = tecnicaVentaService.listarActivas();
        return ResponseEntity.ok(ApiResponse.success(tecnicas, "Técnicas activas obtenidas"));
    }

    /**
     * Lista técnicas por categoría
     * Categorías: APERTURA, DESCUBRIMIENTO, PRESENTACION, OBJECIONES, CIERRE, SEGUIMIENTO
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ApiResponse<List<TecnicaVentaResponse>>> listarPorCategoria(
            @PathVariable String categoria) {
        List<TecnicaVentaResponse> tecnicas = tecnicaVentaService.listarPorCategoria(categoria);
        return ResponseEntity.ok(ApiResponse.success(tecnicas, "Técnicas de " + categoria));
    }

    /**
     * Lista las categorías disponibles
     */
    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<String>>> listarCategorias() {
        List<String> categorias = tecnicaVentaService.listarCategorias();
        return ResponseEntity.ok(ApiResponse.success(categorias, "Categorías obtenidas"));
    }

    /**
     * Obtiene una técnica por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TecnicaVentaResponse>> obtenerPorId(@PathVariable Long id) {
        TecnicaVentaResponse tecnica = tecnicaVentaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(tecnica, "Técnica obtenida"));
    }

    /**
     * Crea una nueva técnica de venta
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TecnicaVentaResponse>> crear(
            @Valid @RequestBody TecnicaVentaRequest request) {
        TecnicaVentaResponse tecnica = tecnicaVentaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tecnica, "Técnica creada exitosamente"));
    }

    /**
     * Crea múltiples técnicas de venta (carga masiva)
     * Útil para cargar técnicas de un libro completo
     */
    @PostMapping("/masivo")
    public ResponseEntity<ApiResponse<List<TecnicaVentaResponse>>> crearMasivo(
            @Valid @RequestBody List<TecnicaVentaRequest> requests) {
        List<TecnicaVentaResponse> tecnicas = tecnicaVentaService.crearMasivo(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tecnicas, tecnicas.size() + " técnicas creadas"));
    }

    /**
     * Actualiza una técnica existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TecnicaVentaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TecnicaVentaRequest request) {
        TecnicaVentaResponse tecnica = tecnicaVentaService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(tecnica, "Técnica actualizada"));
    }

    /**
     * Activa una técnica (el bot la usará)
     */
    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable Long id) {
        tecnicaVentaService.activar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Técnica activada"));
    }

    /**
     * Desactiva una técnica (el bot NO la usará)
     */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        tecnicaVentaService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Técnica desactivada"));
    }

    /**
     * Elimina una técnica
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        tecnicaVentaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Técnica eliminada"));
    }
}
