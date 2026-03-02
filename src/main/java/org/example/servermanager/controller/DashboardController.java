package org.example.servermanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ConversacionRepository conversacionRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(@PathVariable Long empresaId) {
        long totalConversaciones = conversacionRepository.countByEmpresaId(empresaId);
        long totalClientes = clienteRepository.countByEmpresaId(empresaId);
        long totalProductos = productoRepository.countByEmpresaId(empresaId);

        Map<String, Object> stats = Map.of(
            "totalConversaciones", totalConversaciones,
            "conversacionesActivas", 0,
            "totalClientes", totalClientes,
            "totalPedidos", 0,
            "totalIngresos", 0,
            "pedidosPendientes", 0,
            "pedidosHoy", 0,
            "ingresosHoy", 0,
            "totalProductos", totalProductos
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
