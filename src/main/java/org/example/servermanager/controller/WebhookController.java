package org.example.servermanager.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.webhook.WebhookEvent;
import org.example.servermanager.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WhatsAppService whatsAppService;

    /**
     * Endpoint principal para recibir webhooks de Evolution API
     * Configurar en Evolution API: POST https://tu-servidor.com/webhook/evolution
     */
    @PostMapping("/evolution")
    public ResponseEntity<String> handleEvolutionWebhook(@RequestBody WebhookEvent event) {
        log.info("Webhook recibido: evento={}, instancia={}", event.getEvent(), event.getInstance());
        
        try {
            // Procesar el evento de forma asíncrona (no bloquear respuesta)
            whatsAppService.processWebhookEvent(event);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage(), e);
            // Retornamos OK para que Evolution no reintente
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Endpoint alternativo por instancia específica
     * Evolution API v2 envia a: {webhook_url}/{instancia}/{evento}
     */
    @PostMapping("/evolution/{instancia}")
    public ResponseEntity<String> handleEvolutionWebhookByInstance(
            @PathVariable String instancia,
            @RequestBody WebhookEvent event) {
        
        log.info("Webhook recibido para instancia {}: evento={}", instancia, event.getEvent());
        
        // Asegurar que el evento tenga la instancia correcta
        if (event.getInstance() == null) {
            event.setInstance(instancia);
        }
        
        try {
            whatsAppService.processWebhookEvent(event);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error procesando webhook para {}: {}", instancia, e.getMessage());
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Endpoint que matchea el formato de Evolution API v2:
     * POST {webhook_base_url}/{instancia}/{evento}
     * Ejemplo: /webhook/evolution/AromaStudio/messages.upsert
     */
    @PostMapping("/evolution/{instancia}/{evento}")
    public ResponseEntity<String> handleEvolutionWebhookWithEvent(
            @PathVariable String instancia,
            @PathVariable String evento,
            @RequestBody WebhookEvent event) {
        
        log.info("Webhook recibido: instancia={}, evento_url={}, evento_body={}", 
                instancia, evento, event.getEvent());
        
        if (event.getInstance() == null) {
            event.setInstance(instancia);
        }
        if (event.getEvent() == null) {
            event.setEvent(evento.replace("-", "."));
        }
        
        try {
            whatsAppService.processWebhookEvent(event);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error procesando webhook {}/{}: {}", instancia, evento, e.getMessage());
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Verificación de que el webhook está activo (para testing)
     */
    @GetMapping("/evolution/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Webhook activo");
    }
}
