package org.example.servermanager.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    /**
     * Rol del mensaje:
     * - system: Instrucciones del sistema (prompt)
     * - user: Mensaje del usuario
     * - assistant: Respuesta del asistente
     */
    @JsonProperty("role")
    private String role;

    /**
     * Contenido del mensaje
     */
    @JsonProperty("content")
    private String content;

    public static ChatMessage system(String content) {
        return ChatMessage.builder()
                .role("system")
                .content(content)
                .build();
    }

    public static ChatMessage user(String content) {
        return ChatMessage.builder()
                .role("user")
                .content(content)
                .build();
    }

    public static ChatMessage assistant(String content) {
        return ChatMessage.builder()
                .role("assistant")
                .content(content)
                .build();
    }
}
