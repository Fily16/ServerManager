package org.example.servermanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.entity.ConfiguracionSaas;
import org.example.servermanager.service.ConfiguracionSaasService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/configuracion-saas")
@RequiredArgsConstructor
public class ConfiguracionSaasController {

    private final ConfiguracionSaasService configuracionSaasService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConfiguracionSaas>>> listarTodas() {
        List<ConfiguracionSaas> configs = configuracionSaasService.listarTodas();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/{clave}")
    public ResponseEntity<ApiResponse<ConfiguracionSaas>> obtenerPorClave(@PathVariable String clave) {
        ConfiguracionSaas config = configuracionSaasService.obtenerPorClave(clave)
                .orElseThrow(() -> new org.example.servermanager.exception.ResourceNotFoundException(
                        "ConfiguracionSaas", "clave", clave));
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PutMapping("/{clave}")
    public ResponseEntity<ApiResponse<ConfiguracionSaas>> guardar(
            @PathVariable String clave,
            @RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        String descripcion = body.get("descripcion");
        ConfiguracionSaas config = configuracionSaasService.guardar(clave, valor, descripcion);
        return ResponseEntity.ok(ApiResponse.success(config, "Configuración guardada"));
    }

    @DeleteMapping("/{clave}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String clave) {
        configuracionSaasService.eliminar(clave);
        return ResponseEntity.ok(ApiResponse.success(null, "Configuración eliminada"));
    }
}
