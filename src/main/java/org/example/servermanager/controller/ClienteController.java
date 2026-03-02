package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.request.ClienteRequest;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.ClienteResponse;
import org.example.servermanager.dto.response.PageResponse;
import org.example.servermanager.entity.Cliente;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody ClienteRequest request) {
        Cliente cliente = mapToEntity(request);
        Cliente creado = clienteService.crear(empresaId, cliente);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ClienteResponse.fromEntity(creado), "Cliente creado exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> listar(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Cliente> page = clienteService.listarPorEmpresaPaginado(empresaId, pageable);
        PageResponse<ClienteResponse> response = PageResponse.fromPage(page, ClienteResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> buscar(
            @PathVariable Long empresaId,
            @RequestParam String q) {
        List<Cliente> clientes = clienteService.buscarPorTexto(empresaId, q);
        List<ClienteResponse> response = clientes.stream()
                .map(ClienteResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorTelefono(
            @PathVariable Long empresaId,
            @PathVariable String telefono) {
        Cliente cliente = clienteService.obtenerPorTelefono(empresaId, telefono)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "telefono", telefono));
        return ResponseEntity.ok(ApiResponse.success(ClienteResponse.fromEntity(cliente)));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> obtenerTopClientes(
            @PathVariable Long empresaId,
            @RequestParam(defaultValue = "10") int limite) {
        List<Cliente> clientes = clienteService.obtenerTopClientes(empresaId, limite);
        List<ClienteResponse> response = clientes.stream()
                .map(ClienteResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        Cliente cliente = clienteService.obtenerPorId(empresaId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return ResponseEntity.ok(ApiResponse.success(ClienteResponse.fromEntity(cliente)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(
            @PathVariable Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        Cliente cliente = mapToEntity(request);
        Cliente actualizado = clienteService.actualizar(empresaId, id, cliente);
        return ResponseEntity.ok(ApiResponse.success(ClienteResponse.fromEntity(actualizado), "Cliente actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long empresaId,
            @PathVariable Long id) {
        clienteService.eliminar(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cliente eliminado exitosamente"));
    }

    private Cliente mapToEntity(ClienteRequest request) {
        return Cliente.builder()
                .telefono(request.telefono())
                .nombre(request.nombre())
                .email(request.email())
                .distrito(request.distrito())
                .direccion(request.direccion())
                .build();
    }
}
