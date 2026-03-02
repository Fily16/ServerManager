package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.request.ProductoRequest;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.PageResponse;
import org.example.servermanager.dto.response.ProductoResponse;
import org.example.servermanager.entity.Producto;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody ProductoRequest request) {
        Producto producto = mapToEntity(request);
        Producto creado = productoService.crear(empresaId, producto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ProductoResponse.fromEntity(creado), "Producto creado exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductoResponse>>> listar(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Producto> page = productoService.listarPorEmpresaPaginado(empresaId, pageable);
        PageResponse<ProductoResponse> response = PageResponse.fromPage(page, ProductoResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarActivos(@PathVariable Long empresaId) {
        List<Producto> productos = productoService.listarPorEmpresa(empresaId);
        List<ProductoResponse> response = productos.stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/destacados")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarDestacados(@PathVariable Long empresaId) {
        List<Producto> productos = productoService.listarDestacados(empresaId);
        List<ProductoResponse> response = productos.stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> buscar(
            @PathVariable Long empresaId,
            @RequestParam String q) {
        List<Producto> productos = productoService.buscar(empresaId, q);
        List<ProductoResponse> response = productos.stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/por-precio")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> buscarPorPrecio(
            @PathVariable Long empresaId,
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        List<Producto> productos = productoService.buscarPorRangoPrecio(empresaId, min, max);
        List<ProductoResponse> response = productos.stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarPorCategoria(
            @PathVariable Long empresaId,
            @PathVariable Long categoriaId) {
        List<Producto> productos = productoService.listarPorCategoria(empresaId, categoriaId);
        List<ProductoResponse> response = productos.stream()
                .map(ProductoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorId(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        Producto producto = productoService.obtenerPorId(empresaId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        return ResponseEntity.ok(ApiResponse.success(ProductoResponse.fromEntity(producto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        Producto producto = mapToEntity(request);
        Producto actualizado = productoService.actualizar(empresaId, id, producto);
        return ResponseEntity.ok(ApiResponse.success(ProductoResponse.fromEntity(actualizado), "Producto actualizado exitosamente"));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Void>> actualizarStock(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        productoService.actualizarStock(empresaId, id, cantidad);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        productoService.eliminar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Producto eliminado exitosamente"));
    }

    private Producto mapToEntity(ProductoRequest request) {
        return Producto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precio(request.precio())
                .precioOferta(request.precioOferta())
                .imagenUrl(request.imagenUrl())
                .tieneStock(request.tieneStock() != null ? request.tieneStock() : false)
                .stockActual(request.stockActual() != null ? request.stockActual() : 0)
                .esDestacado(request.esDestacado() != null ? request.esDestacado() : false)
                .tags(request.tags())
                .activo(request.activo() != null ? request.activo() : true)
                .build();
    }
}
