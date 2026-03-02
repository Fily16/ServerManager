package org.example.servermanager.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("object")
    private String object;

    @JsonProperty("created")
    private Long created;

    @JsonProperty("model")
    private String model;

    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("usage")
    private Usage usage;

    /**
     * Obtiene el texto de la respuesta
     */
    public String getResponseText() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Choice firstChoice = choices.get(0);
        if (firstChoice.getMessage() == null) {
            return null;
        }
        return firstChoice.getMessage().getContent();
    }

    /**
     * Obtiene el total de tokens usados
     */
    public Integer getTotalTokens() {
        return usage != null ? usage.getTotalTokens() : null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        @JsonProperty("index")
        private Integer index;

        @JsonProperty("message")
        private ChatMessage message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
