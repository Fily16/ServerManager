package org.example.servermanager.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageContent {

    /**
     * Texto del mensaje (para mensajes de texto simple)
     */
    @JsonProperty("conversation")
    private String conversation;

    /**
     * Texto extendido (para mensajes con formato)
     */
    @JsonProperty("extendedTextMessage")
    private ExtendedTextMessage extendedTextMessage;

    /**
     * Mensaje de imagen
     */
    @JsonProperty("imageMessage")
    private MediaMessage imageMessage;

    /**
     * Mensaje de audio
     */
    @JsonProperty("audioMessage")
    private MediaMessage audioMessage;

    /**
     * Mensaje de documento
     */
    @JsonProperty("documentMessage")
    private MediaMessage documentMessage;

    /**
     * Mensaje de ubicación
     */
    @JsonProperty("locationMessage")
    private LocationMessage locationMessage;

    /**
     * Obtiene el texto del mensaje, sin importar el tipo
     */
    public String getText() {
        if (conversation != null && !conversation.isEmpty()) {
            return conversation;
        }
        if (extendedTextMessage != null && extendedTextMessage.getText() != null) {
            return extendedTextMessage.getText();
        }
        if (imageMessage != null && imageMessage.getCaption() != null) {
            return imageMessage.getCaption();
        }
        if (documentMessage != null && documentMessage.getCaption() != null) {
            return documentMessage.getCaption();
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtendedTextMessage {
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MediaMessage {
        private String url;
        private String mimetype;
        private String caption;
        private String fileName;
        private Long fileLength;
        private String mediaKey;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationMessage {
        private Double degreesLatitude;
        private Double degreesLongitude;
        private String name;
        private String address;
    }
}
