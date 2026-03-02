package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.request.DetallePedidoRequest;
import org.example.servermanager.dto.request.PedidoRequest;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.PageResponse;
import org.example.servermanager.dto.response.PedidoResponse;
import org.example.servermanager.entity.DetallePedido;
import org.example.servermanager.entity.Pedido;
import org.example.servermanager.enums.EstadoPedido;
import org.example.servermanager.enums.MetodoPago;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.PedidoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody PedidoRequest request) {
        Pedido pedido = mapToEntity(request);
        
        if (request.detalles() != null && !request.detalles().isEmpty()) {
            for (DetallePedidoRequest detalleReq : request.detalles()) {
                pedido.addDetalle(mapDetalleToEntity(detalleReq));
            }
        }
        
        Pedido creado = pedidoService.crear(empresaId, request.conversacionId(), pedido);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(PedidoResponse.fromEntity(creado), "Pedido creado exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PedidoResponse>>> listar(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Pedido> page = pedidoService.listarPorEmpresaPaginado(empresaId, pageable);
        PageResponse<PedidoResponse> response = PageResponse.fromPage(page, PedidoResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPorEstado(
            @PathVariable Long empresaId,
            @PathVariable EstadoPedido estado) {
        List<Pedido> pedidos = pedidoService.listarPorEstado(empresaId, estado);
        List<PedidoResponse> response = pedidos.stream()
                .map(PedidoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPendientes(@PathVariable Long empresaId) {
        List<Pedido> pedidos = pedidoService.listarPendientes(empresaId);
        List<PedidoResponse> response = pedidos.stream()
                .map(PedidoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/por-fechas")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPorFechas(
            @PathVariable Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<Pedido> pedidos = pedidoService.listarPorFechas(empresaId, desde, hasta);
        List<PedidoResponse> response = pedidos.stream()
                .map(PedidoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/buscar-por-monto")
    public ResponseEntity<ApiResponse<PedidoResponse>> buscarPorMonto(
            @PathVariable Long empresaId,
            @RequestParam BigDecimal monto) {
        Pedido pedido = pedidoService.buscarPedidoPendientePorMonto(empresaId, monto)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido pendiente", "monto", monto));
        return ResponseEntity.ok(ApiResponse.success(PedidoResponse.fromEntity(pedido)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> obtenerPorId(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        Pedido pedido = pedidoService.obtenerPorId(empresaId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        return ResponseEntity.ok(ApiResponse.success(PedidoResponse.fromEntity(pedido)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> actualizar(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody PedidoRequest request) {
        Pedido pedido = mapToEntity(request);
        Pedido actualizado = pedidoService.actualizar(empresaId, id, pedido);
        return ResponseEntity.ok(ApiResponse.success(PedidoResponse.fromEntity(actualizado), "Pedido actualizado exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestParam EstadoPedido estado) {
        pedidoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success(null, "Estado actualizado a " + estado));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<Void>> marcarComoPagado(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestParam MetodoPago metodoPago) {
        pedidoService.marcarComoPagado(id, metodoPago);
        return ResponseEntity.ok(ApiResponse.success(null, "Pedido marcado como pagado"));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<Void>> cancelar(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        pedidoService.cancelar(empresaId, id, motivo != null ? motivo : "Sin motivo especificado");
        return ResponseEntity.ok(ApiResponse.success(null, "Pedido cancelado"));
    }

    @GetMapping("/generar-monto-unico")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> generarMontoUnico(
            @PathVariable Long empresaId,
            @RequestParam BigDecimal montoBase) {
        BigDecimal montoUnico = pedidoService.generarMontoUnico(empresaId, montoBase);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "montoBase", montoBase,
                "montoUnico", montoUnico
        )));
    }

    private Pedido mapToEntity(PedidoRequest request) {
        return Pedido.builder()
                .telefonoCliente(request.telefonoCliente())
                .nombreCliente(request.nombreCliente())
                .direccionEnvio(request.direccionEnvio())
                .costoEnvio(request.costoEnvio() != null ? request.costoEnvio() : BigDecimal.ZERO)
                .descuento(request.descuento() != null ? request.descuento() : BigDecimal.ZERO)
                .metodoPago(request.metodoPago())
                .notas(request.notas())
                .build();
    }

    private DetallePedido mapDetalleToEntity(DetallePedidoRequest request) {
        DetallePedido detalle = DetallePedido.builder()
                .nombreProducto(request.nombreProducto())
                .precioUnitario(request.precioUnitario())
                .cantidad(request.cantidad())
                .notas(request.notas())
                .build();
        detalle.calcularSubtotal();
        return detalle;
    }
}
