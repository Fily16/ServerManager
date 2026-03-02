package org.example.servermanager.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEvent {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Tipo de evento:
     * - messages.upsert (mensaje nuevo)
     * - messages.update (actualización de estado)
     * - connection.update (estado de conexión)
     * - qrcode.updated (QR actualizado)
     * - chats-set, messages-set (sincronización inicial - data es array)
     */
    @JsonProperty("event")
    private String event;

    /**
     * Nombre de la instancia de Evolution API
     */
    @JsonProperty("instance")
    private String instance;

    /**
     * Datos del webhook - puede ser objeto o array dependiendo del evento.
     * Para messages.upsert es un objeto MessageData.
     * Para chats-set y messages-set es un array.
     */
    @JsonProperty("data")
    private JsonNode rawData;

    /**
     * API Key de la instancia (para validación)
     */
    @JsonProperty("apikey")
    private String apikey;

    /**
     * Obtiene MessageData si data es un objeto (no array)
     */
    @JsonIgnore
    public MessageData getData() {
        if (rawData == null || rawData.isArray() || rawData.isNull()) {
            return null;
        }
        try {
            return mapper.treeToValue(rawData, MessageData.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si es un evento de mensaje nuevo
     */
    public boolean isNewMessage() {
        return "messages.upsert".equals(event);
    }

    /**
     * Verifica si es una actualización de conexión
     */
    public boolean isConnectionUpdate() {
        return "connection.update".equals(event);
    }

    /**
     * Verifica si es un evento de sincronización (data es array)
     */
    public boolean isSyncEvent() {
        return event != null && (event.endsWith("-set") || event.endsWith(".set"));
    }

    /**
     * Verifica si es un mensaje entrante válido para procesar
     */
    public boolean isValidIncomingMessage() {
        MessageData data = getData();
        return isNewMessage()
            && data != null
            && data.isIncoming()
            && !data.isGroup()
            && data.getTelefono() != null;
    }
}
