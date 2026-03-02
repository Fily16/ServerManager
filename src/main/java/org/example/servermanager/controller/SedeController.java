package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.request.SedeRequest;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.SedeResponse;
import org.example.servermanager.entity.Sede;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.SedeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/sedes")
@RequiredArgsConstructor
public class SedeController {

    private final SedeService sedeService;

    @PostMapping
    public ResponseEntity<ApiResponse<SedeResponse>> crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody SedeRequest request) {
        Sede sede = mapToEntity(request);
        Sede creada = sedeService.crear(empresaId, sede);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SedeResponse.fromEntity(creada), "Sede creada exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SedeResponse>>> listar(@PathVariable Long empresaId) {
        List<Sede> sedes = sedeService.listarPorEmpresa(empresaId);
        List<SedeResponse> response = sedes.stream()
                .map(SedeResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<SedeResponse>>> listarActivas(@PathVariable Long empresaId) {
        List<Sede> sedes = sedeService.listarActivasPorEmpresa(empresaId);
        List<SedeResponse> response = sedes.stream()
                .map(SedeResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/principal")
    public ResponseEntity<ApiResponse<SedeResponse>> obtenerPrincipal(@PathVariable Long empresaId) {
        Sede sede = sedeService.obtenerSedePrincipal(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede principal", "empresaId", empresaId));
        return ResponseEntity.ok(ApiResponse.success(SedeResponse.fromEntity(sede)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SedeResponse>> obtenerPorId(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        Sede sede = sedeService.obtenerPorId(empresaId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede", "id", id));
        return ResponseEntity.ok(ApiResponse.success(SedeResponse.fromEntity(sede)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SedeResponse>> actualizar(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody SedeRequest request) {
        Sede sede = mapToEntity(request);
        Sede actualizada = sedeService.actualizar(empresaId, id, sede);
        return ResponseEntity.ok(ApiResponse.success(SedeResponse.fromEntity(actualizada), "Sede actualizada exitosamente"));
    }

    @PatchMapping("/{id}/principal")
    public ResponseEntity<ApiResponse<Void>> marcarComoPrincipal(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        sedeService.establecerComoPrincipal(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sede marcada como principal"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        sedeService.eliminar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sede eliminada exitosamente"));
    }

    private Sede mapToEntity(SedeRequest request) {
        return Sede.builder()
                .nombre(request.nombre())
                .direccion(request.direccion())
                .distrito(request.distrito())
                .ciudad(request.ciudad())
                .referencia(request.referencia())
                .telefono(request.telefono())
                .horarioAtencion(request.horarioAtencion())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .esPrincipal(request.esPrincipal() != null ? request.esPrincipal() : false)
                .activo(request.activo() != null ? request.activo() : true)
                .build();
    }
}
