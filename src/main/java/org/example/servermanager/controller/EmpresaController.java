package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.request.EmpresaRequest;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.EmpresaResponse;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.enums.Plan;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.EmpresaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaResponse>> crear(@Valid @RequestBody EmpresaRequest request) {
        Empresa empresa = mapToEntity(request);
        Empresa creada = empresaService.crear(empresa);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EmpresaResponse.fromEntity(creada), "Empresa creada exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmpresaResponse>>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Plan plan) {
        
        List<Empresa> empresas;
        if (plan != null) {
            empresas = empresaService.listarPorPlan(plan);
        } else if (Boolean.TRUE.equals(activo)) {
            empresas = empresaService.listarActivas();
        } else {
            empresas = empresaService.listarTodas();
        }

        List<EmpresaResponse> response = empresas.stream()
                .map(EmpresaResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> obtenerPorId(@PathVariable Long id) {
        Empresa empresa = empresaService.obtenerPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", id));
        return ResponseEntity.ok(ApiResponse.success(EmpresaResponse.fromEntity(empresa)));
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> obtenerPorRuc(@PathVariable String ruc) {
        Empresa empresa = empresaService.obtenerPorRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "ruc", ruc));
        return ResponseEntity.ok(ApiResponse.success(EmpresaResponse.fromEntity(empresa)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaRequest request) {
        Empresa empresa = mapToEntity(request);
        Empresa actualizada = empresaService.actualizar(id, empresa);
        return ResponseEntity.ok(ApiResponse.success(EmpresaResponse.fromEntity(actualizada), "Empresa actualizada exitosamente"));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable Long id) {
        empresaService.activar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Empresa activada exitosamente"));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        empresaService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Empresa desactivada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        empresaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Empresa eliminada exitosamente"));
    }

    private Empresa mapToEntity(EmpresaRequest request) {
        return Empresa.builder()
                .nombre(request.nombre())
                .ruc(request.ruc())
                .email(request.email())
                .telefono(request.telefono())
                .logoUrl(request.logoUrl())
                .direccionFiscal(request.direccionFiscal())
                .plan(request.plan() != null ? request.plan() : Plan.BASICO)
                .build();
    }
}
