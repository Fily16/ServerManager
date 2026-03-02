package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.request.ConfiguracionBotRequest;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.ConfiguracionBotResponse;
import org.example.servermanager.entity.ConfiguracionBot;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.service.ConfiguracionBotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/configuracion-bot")
@RequiredArgsConstructor
public class ConfiguracionBotController {

    private final ConfiguracionBotService configuracionBotService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConfiguracionBotResponse>> crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody ConfiguracionBotRequest request) {
        ConfiguracionBot config = mapToEntity(request);
        ConfiguracionBot creada = configuracionBotService.crear(empresaId, config);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ConfiguracionBotResponse.fromEntity(creada), "Configuración creada exitosamente"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ConfiguracionBotResponse>> obtener(@PathVariable Long empresaId) {
        ConfiguracionBot config = configuracionBotService.obtenerPorEmpresaId(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("ConfiguracionBot", "empresaId", empresaId));
        return ResponseEntity.ok(ApiResponse.success(ConfiguracionBotResponse.fromEntity(config)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ConfiguracionBotResponse>> actualizar(
            @PathVariable Long empresaId,
            @Valid @RequestBody ConfiguracionBotRequest request) {
        ConfiguracionBot config = mapToEntity(request);
        ConfiguracionBot actualizada = configuracionBotService.actualizar(empresaId, config);
        return ResponseEntity.ok(ApiResponse.success(ConfiguracionBotResponse.fromEntity(actualizada), "Configuración actualizada exitosamente"));
    }

    @PatchMapping("/activar")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable Long empresaId) {
        configuracionBotService.activar(empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Bot activado exitosamente"));
    }

    @PatchMapping("/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long empresaId) {
        configuracionBotService.desactivar(empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Bot desactivado exitosamente"));
    }

    @PatchMapping("/verificacion-pagos")
    public ResponseEntity<ApiResponse<Void>> toggleVerificacionPagos(
            @PathVariable Long empresaId,
            @RequestParam boolean activo) {
        configuracionBotService.toggleVerificacionPagos(empresaId, activo);
        String mensaje = activo ? "Verificación de pagos activada" : "Verificación de pagos desactivada";
        return ResponseEntity.ok(ApiResponse.success(null, mensaje));
    }

    private ConfiguracionBot mapToEntity(ConfiguracionBotRequest request) {
        return ConfiguracionBot.builder()
                .evolutionApiUrl(request.evolutionApiUrl())
                .evolutionApiKey(request.evolutionApiKey())
                .evolutionInstancia(request.evolutionInstancia())
                .numeroWhatsapp(request.numeroWhatsapp())
                .nombreBot(request.nombreBot())
                .mensajeBienvenida(request.mensajeBienvenida())
                .promptSistema(request.promptSistema())
                .tonoConversacion(request.tonoConversacion())
                .modeloAi(request.modeloAi())
                .verificacionPagosActivo(request.verificacionPagosActivo())
                .emailNotificacionesPago(request.emailNotificacionesPago())
                .emailPasswordApp(request.emailPasswordApp())
                .linkGrupoConsolidado(request.linkGrupoConsolidado())
                .linkCatalogo(request.linkCatalogo())
                .tiempoEntrega(request.tiempoEntrega())
                .linkTiktok(request.linkTiktok())
                .promptCampana(request.promptCampana())
                .horarioInicio(request.horarioInicio())
                .horarioFin(request.horarioFin())
                .mensajeFueraHorario(request.mensajeFueraHorario())
                .autoRespuesta(request.autoRespuesta())
                .activo(request.activo())
                .build();
    }
}
