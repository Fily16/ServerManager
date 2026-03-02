package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.request.CategoriaProductoRequest;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.CategoriaProductoResponse;
import org.example.servermanager.entity.CategoriaProducto;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.CategoriaProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/categorias")
@RequiredArgsConstructor
public class CategoriaProductoController {

    private final CategoriaProductoService categoriaService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaProductoResponse>> crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody CategoriaProductoRequest request) {
        CategoriaProducto categoria = mapToEntity(request);
        CategoriaProducto creada = categoriaService.crear(empresaId, categoria);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(CategoriaProductoResponse.fromEntity(creada), "Categoría creada exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaProductoResponse>>> listar(@PathVariable Long empresaId) {
        List<CategoriaProducto> categorias = categoriaService.listarPorEmpresa(empresaId);
        List<CategoriaProductoResponse> response = categorias.stream()
                .map(CategoriaProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<CategoriaProductoResponse>>> listarActivas(@PathVariable Long empresaId) {
        List<CategoriaProducto> categorias = categoriaService.listarActivasPorEmpresa(empresaId);
        List<CategoriaProductoResponse> response = categorias.stream()
                .map(CategoriaProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaProductoResponse>> obtenerPorId(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        CategoriaProducto categoria = categoriaService.obtenerPorId(empresaId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
        return ResponseEntity.ok(ApiResponse.success(CategoriaProductoResponse.fromEntity(categoria)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaProductoResponse>> actualizar(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody CategoriaProductoRequest request) {
        CategoriaProducto categoria = mapToEntity(request);
        CategoriaProducto actualizada = categoriaService.actualizar(empresaId, id, categoria);
        return ResponseEntity.ok(ApiResponse.success(CategoriaProductoResponse.fromEntity(actualizada), "Categoría actualizada exitosamente"));
    }

    @PatchMapping("/{id}/orden")
    public ResponseEntity<ApiResponse<Void>> actualizarOrden(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestParam Integer orden) {
        categoriaService.reordenar(empresaId, id, orden);
        return ResponseEntity.ok(ApiResponse.success(null, "Orden actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        categoriaService.eliminar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Categoría eliminada exitosamente"));
    }

    private CategoriaProducto mapToEntity(CategoriaProductoRequest request) {
        return CategoriaProducto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .orden(request.orden() != null ? request.orden() : 0)
                .activo(request.activo() != null ? request.activo() : true)
                .build();
    }
}
