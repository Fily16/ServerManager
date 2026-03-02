package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.service.ConfiguracionSaasService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsService {

    private static final String ELEVENLABS_API_URL = "https://api.elevenlabs.io/v1/text-to-speech/";

    private final ConfiguracionSaasService configuracionSaasService;
    private final RestTemplate restTemplate;

    /**
     * Convierte texto a audio usando ElevenLabs TTS.
     * Retorna los bytes del audio en formato MP3, o null si falla.
     */
    public byte[] convertirTextoAAudio(String texto) {
        try {
            String apiKey = configuracionSaasService.obtenerElevenLabsApiKey();
            String voiceId = configuracionSaasService.obtenerElevenLabsVoiceId();

            if (apiKey.isBlank() || voiceId.isBlank()) {
                log.warn("ElevenLabs no configurado (API key o voice ID vacios)");
                return null;
            }

            String url = ELEVENLABS_API_URL + voiceId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", apiKey);
            headers.setAccept(java.util.List.of(MediaType.valueOf("audio/mpeg")));

            Map<String, Object> body = Map.of(
                    "text", texto,
                    "model_id", "eleven_multilingual_v2",
                    "voice_settings", Map.of(
                            "stability", 0.5,
                            "similarity_boost", 0.75
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("ElevenLabs: audio generado ({} bytes) para texto de {} chars",
                        response.getBody().length, texto.length());
                return response.getBody();
            }

            log.warn("ElevenLabs: respuesta no exitosa: {}", response.getStatusCode());
            return null;

        } catch (Exception e) {
            log.error("Error generando audio con ElevenLabs: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si ElevenLabs esta configurado y disponible.
     */
    public boolean isConfigured() {
        try {
            String apiKey = configuracionSaasService.obtenerElevenLabsApiKey();
            String voiceId = configuracionSaasService.obtenerElevenLabsVoiceId();
            return !apiKey.isBlank() && !voiceId.isBlank();
        } catch (Exception e) {
            return false;
        }
    }
}
