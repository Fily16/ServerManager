package org.example.servermanager.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<ChatMessage> messages;

    @JsonProperty("max_tokens")
    @Builder.Default
    private Integer maxTokens = 500;

    @JsonProperty("temperature")
    @Builder.Default
    private Double temperature = 0.7;

    @JsonProperty("presence_penalty")
    @Builder.Default
    private Double presencePenalty = 0.1;

    @JsonProperty("frequency_penalty")
    @Builder.Default
    private Double frequencyPenalty = 0.1;
}
