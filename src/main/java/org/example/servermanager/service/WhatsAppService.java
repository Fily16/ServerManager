package org.example.servermanager.service;

import org.example.servermanager.dto.webhook.SendMessageResponse;
import org.example.servermanager.dto.webhook.WebhookEvent;

public interface WhatsAppService {

    /**
     * Procesa un evento de webhook de Evolution API
     * @param event Evento recibido
     */
    void processWebhookEvent(WebhookEvent event);

    /**
     * Envía un mensaje de texto por WhatsApp
     * @param empresaId ID de la empresa
     * @param telefono Número destino
     * @param mensaje Texto del mensaje
     * @return Respuesta de Evolution API
     */
    SendMessageResponse enviarMensaje(Long empresaId, String telefono, String mensaje);

    /**
     * Envía un mensaje respondiendo a otro
     * @param empresaId ID de la empresa
     * @param telefono Número destino
     * @param mensaje Texto del mensaje
     * @param messageIdOriginal ID del mensaje al que responde
     * @return Respuesta de Evolution API
     */
    SendMessageResponse enviarRespuesta(Long empresaId, String telefono, String mensaje, String messageIdOriginal);

    /**
     * Verifica si una instancia de Evolution API está conectada
     * @param empresaId ID de la empresa
     * @return true si está conectada
     */
    boolean isInstanceConnected(Long empresaId);
}
