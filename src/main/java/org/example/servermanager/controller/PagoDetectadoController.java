package org.example.servermanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.PagoDetectadoResponse;
import org.example.servermanager.dto.response.PageResponse;
import org.example.servermanager.entity.PagoDetectado;
import org.example.servermanager.enums.Plataforma;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.PagoDetectadoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/pagos-detectados")
@RequiredArgsConstructor
public class PagoDetectadoController {

    private final PagoDetectadoService pagoDetectadoService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PagoDetectadoResponse>>> listar(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PagoDetectado> pagos = pagoDetectadoService.listarPorEmpresaPaginado(empresaId, pageable);
        PageResponse<PagoDetectadoResponse> response = PageResponse.fromPage(pagos, PagoDetectadoResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<ApiResponse<List<PagoDetectadoResponse>>> listarPendientes(@PathVariable Long empresaId) {
        List<PagoDetectado> pagos = pagoDetectadoService.listarNoProcesados(empresaId);
        List<PagoDetectadoResponse> response = pagos.stream()
                .map(PagoDetectadoResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoDetectadoResponse>> obtenerPorId(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        PagoDetectado pago = pagoDetectadoService.obtenerPorId(empresaId, id)
                .orElseThrow(() -> new ResourceNotFoundException("PagoDetectado", "id", id));
        return ResponseEntity.ok(ApiResponse.success(PagoDetectadoResponse.fromEntity(pago)));
    }

    @PostMapping("/procesar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> procesarPago(
            @PathVariable Long empresaId,
            @RequestParam BigDecimal monto,
            @RequestParam String nombreCliente,
            @RequestParam Plataforma plataforma) {

        PagoDetectado pago = pagoDetectadoService.procesarYAsociar(empresaId, monto, nombreCliente, plataforma);
        boolean matchEncontrado = pago.getPedido() != null;

        Map<String, Object> resultado = Map.of(
                "pago", PagoDetectadoResponse.fromEntity(pago),
                "matchAutomatico", matchEncontrado,
                "pedidoId", matchEncontrado ? pago.getPedido().getId() : 0L,
                "mensaje", matchEncontrado
                        ? "Pago asociado automáticamente al pedido #" + pago.getPedido().getId()
                        : "Pago registrado pero sin pedido pendiente encontrado"
        );

        return ResponseEntity.ok(ApiResponse.success(resultado));
    }

    @PatchMapping("/{id}/asociar-pedido/{pedidoId}")
    public ResponseEntity<ApiResponse<Void>> asociarPedido(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @PathVariable Long pedidoId) {
        pagoDetectadoService.asociarAPedido(id, pedidoId);
        return ResponseEntity.ok(ApiResponse.success(null, "Pago asociado al pedido exitosamente"));
    }
}
