package org.example.servermanager.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.servermanager.enums.TipoContenido;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageData {

    /**
     * Identificador del mensaje
     */
    @JsonProperty("key")
    private MessageKey key;

    /**
     * Nombre del contacto que envía
     */
    @JsonProperty("pushName")
    private String pushName;

    /**
     * Contenido del mensaje
     */
    @JsonProperty("message")
    private MessageContent message;

    /**
     * Tipo de mensaje: conversation, imageMessage, audioMessage, etc.
     */
    @JsonProperty("messageType")
    private String messageType;

    /**
     * Timestamp del mensaje (epoch en segundos)
     */
    @JsonProperty("messageTimestamp")
    private Long messageTimestamp;

    /**
     * Obtiene el texto del mensaje
     */
    public String getTexto() {
        return message != null ? message.getText() : null;
    }

    /**
     * Obtiene el número de teléfono del remitente
     */
    public String getTelefono() {
        return key != null ? key.getPhoneNumber() : null;
    }

    /**
     * Obtiene el nombre del remitente
     */
    public String getNombre() {
        return pushName;
    }

    /**
     * Obtiene el ID del mensaje de WhatsApp
     */
    public String getWhatsAppMessageId() {
        return key != null ? key.getId() : null;
    }

    /**
     * Verifica si es un mensaje entrante (no enviado por nosotros)
     */
    public boolean isIncoming() {
        return key != null && !Boolean.TRUE.equals(key.getFromMe());
    }

    /**
     * Verifica si es un mensaje de grupo
     */
    public boolean isGroup() {
        return key != null && key.isGroupMessage();
    }

    /**
     * Verifica si el JID es formato LID (Linked ID) - no se puede responder
     */
    public boolean isLidFormat() {
        return key != null && key.isLidFormat();
    }

    /**
     * Determina el tipo de contenido
     */
    public TipoContenido getTipoContenido() {
        if (messageType == null) return TipoContenido.TEXTO;
        
        return switch (messageType.toLowerCase()) {
            case "imagemessage" -> TipoContenido.IMAGEN;
            case "audiomessage", "ptvaudiomessage" -> TipoContenido.AUDIO;
            case "documentmessage" -> TipoContenido.DOCUMENTO;
            case "locationmessage" -> TipoContenido.UBICACION;
            default -> TipoContenido.TEXTO;
        };
    }

    /**
     * Obtiene la URL del media si existe
     */
    public String getMediaUrl() {
        if (message == null) return null;
        
        if (message.getImageMessage() != null) {
            return message.getImageMessage().getUrl();
        }
        if (message.getAudioMessage() != null) {
            return message.getAudioMessage().getUrl();
        }
        if (message.getDocumentMessage() != null) {
            return message.getDocumentMessage().getUrl();
        }
        return null;
    }
}
