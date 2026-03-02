package org.example.servermanager.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.webhook.SendMessageResponse;
import org.example.servermanager.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    /**
     * Enviar mensaje de texto a un número
     */
    @PostMapping("/enviar")
    public ResponseEntity<ApiResponse<SendMessageResponse>> enviarMensaje(
            @PathVariable Long empresaId,
            @RequestParam String telefono,
            @RequestParam String mensaje) {
        
        log.info("Enviando mensaje manual a {} para empresa {}", telefono, empresaId);
        
        SendMessageResponse response = whatsAppService.enviarMensaje(empresaId, telefono, mensaje);
        
        if (response != null && response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response, "Mensaje enviado correctamente"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No se pudo enviar el mensaje"));
        }
    }

    /**
     * Verificar estado de conexión de la instancia
     */
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<Boolean>> verificarConexion(@PathVariable Long empresaId) {
        boolean connected = whatsAppService.isInstanceConnected(empresaId);
        
        String mensaje = connected ? "Instancia conectada" : "Instancia desconectada";
        return ResponseEntity.ok(ApiResponse.success(connected, mensaje));
    }
}
