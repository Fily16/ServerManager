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
public class SendMessageResponse {

    @JsonProperty("key")
    private MessageKey key;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private MessageContent message;

    /**
     * Obtiene el ID del mensaje enviado
     */
    public String getMessageId() {
        return key != null ? key.getId() : null;
    }

    /**
     * Verifica si el mensaje se envió correctamente
     */
    public boolean isSuccess() {
        return key != null && key.getId() != null;
    }
}
