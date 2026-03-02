package org.example.servermanager.dto.webhook;

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
public class SendMessageRequest {

    /**
     * Número destino en formato: 51999888777
     */
    @JsonProperty("number")
    private String number;

    /**
     * Texto del mensaje
     */
    @JsonProperty("text")
    private String text;

    /**
     * Mensaje citando otro (reply)
     */
    @JsonProperty("quoted")
    private QuotedMessage quoted;

    /**
     * Crea un mensaje de texto simple
     */
    public static SendMessageRequest text(String number, String text) {
        return SendMessageRequest.builder()
                .number(formatNumber(number))
                .text(text)
                .build();
    }

    /**
     * Crea un mensaje respondiendo a otro
     */
    public static SendMessageRequest reply(String number, String text, String messageId) {
        return SendMessageRequest.builder()
                .number(formatNumber(number))
                .text(text)
                .quoted(QuotedMessage.builder().messageId(messageId).build())
                .build();
    }

    /**
     * Asegura formato correcto del número (sin +, sin espacios).
     * Si ya es un JID completo (contiene @), lo deja tal cual.
     */
    private static String formatNumber(String number) {
        if (number == null) return null;
        // Si ya es un JID completo (@s.whatsapp.net o @lid), no modificar
        if (number.contains("@")) return number;
        String cleaned = number.replaceAll("[^0-9]", "");
        // Si empieza con 9 y tiene 9 dígitos, agregar 51 (Perú)
        if (cleaned.length() == 9 && cleaned.startsWith("9")) {
            cleaned = "51" + cleaned;
        }
        return cleaned;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotedMessage {
        @JsonProperty("key")
        private MessageKeySimple key;

        @JsonProperty("messageId")
        private String messageId;

        public static QuotedMessage of(String messageId) {
            return QuotedMessage.builder().messageId(messageId).build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageKeySimple {
        @JsonProperty("id")
        private String id;
    }
}
