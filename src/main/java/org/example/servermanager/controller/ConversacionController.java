package org.example.servermanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.ConversacionResponse;
import org.example.servermanager.dto.response.PageResponse;
import org.example.servermanager.entity.Conversacion;
import org.example.servermanager.enums.EstadoConversacion;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.ConversacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/conversaciones")
@RequiredArgsConstructor
public class ConversacionController {

    private final ConversacionService conversacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ConversacionResponse>>> listar(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Conversacion> page = conversacionService.listarPorEmpresaPaginado(empresaId, pageable);
        PageResponse<ConversacionResponse> response = PageResponse.fromPage(page, ConversacionResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<ConversacionResponse>>> listarActivas(@PathVariable Long empresaId) {
        List<Conversacion> conversaciones = conversacionService.listarActivas(empresaId);
        List<ConversacionResponse> response = conversaciones.stream()
                .map(ConversacionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<ConversacionResponse>>> listarPorEstado(
            @PathVariable Long empresaId,
            @PathVariable EstadoConversacion estado) {
        List<Conversacion> conversaciones = conversacionService.listarPorEstado(empresaId, estado);
        List<ConversacionResponse> response = conversaciones.stream()
                .map(ConversacionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<ApiResponse<ConversacionResponse>> obtenerActiva(
            @PathVariable Long empresaId,
            @PathVariable String telefono) {
        Conversacion conversacion = conversacionService.obtenerActiva(empresaId, telefono)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación activa", "telefono", telefono));
        return ResponseEntity.ok(ApiResponse.success(ConversacionResponse.fromEntity(conversacion)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversacionResponse>> obtenerPorId(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        Conversacion conversacion = conversacionService.obtenerPorId(empresaId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación", "id", id));
        return ResponseEntity.ok(ApiResponse.success(ConversacionResponse.fromEntity(conversacion)));
    }

    @GetMapping("/{id}/completa")
    public ResponseEntity<ApiResponse<ConversacionResponse>> obtenerConMensajes(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        Conversacion conversacion = conversacionService.obtenerPorIdConMensajes(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación", "id", id));
        return ResponseEntity.ok(ApiResponse.success(ConversacionResponse.fromEntity(conversacion, true)));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<ApiResponse<Void>> cerrar(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        conversacionService.cerrar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Conversación cerrada exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @RequestParam EstadoConversacion estado) {
        conversacionService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.success(null, "Estado actualizado a " + estado));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticas(@PathVariable Long empresaId) {
        Map<String, Object> stats = conversacionService.obtenerEstadisticas(empresaId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
