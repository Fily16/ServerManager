package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.openai.ChatCompletionRequest;
import org.example.servermanager.dto.openai.ChatCompletionResponse;
import org.example.servermanager.dto.openai.ChatMessage;
import org.example.servermanager.dto.webhook.SendMessageResponse;
import org.example.servermanager.entity.*;
import org.example.servermanager.enums.EstadoConversacion;
import org.example.servermanager.enums.TipoRemitente;
import org.example.servermanager.repository.ConversacionRepository;
import org.example.servermanager.repository.MensajeRepository;
import org.example.servermanager.service.ConfiguracionSaasService;
import org.example.servermanager.service.WhatsAppService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maneja las conversaciones con contactos de campana.
 * Cuando alguien de un grupo te responde, este servicio genera
 * la respuesta usando la info de tu negocio + tecnicas de venta.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CampanaConversacionService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String TOKEN_ENVIAR_LINK = "[ENVIAR_LINK]";

    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final ConfiguracionSaasService configuracionSaasService;
    private final WhatsAppService whatsAppService;
    private final RestTemplate restTemplate;

    /**
     * Procesa un mensaje entrante de un contacto de campana.
     * Genera respuesta con IA y detecta si hay que enviar link del grupo.
     */
    public void procesarRespuestaCampana(Empresa empresa, Campana campana,
                                          String telefono, String nombre,
                                          String textoRecibido, String whatsappMessageId) {

        log.info("Campana {}: respuesta de {} ({}): {}", campana.getId(), nombre, telefono,
                textoRecibido != null ? textoRecibido.substring(0, Math.min(textoRecibido.length(), 50)) : "[vacio]");

        // 1. Buscar o crear conversacion para este contacto de campana
        Conversacion conversacion = findOrCreateConversacionCampana(empresa, telefono, nombre);

        // 2. Guardar mensaje entrante
        if (whatsappMessageId != null && mensajeRepository.existsByWhatsappMessageId(whatsappMessageId)) {
            log.debug("Mensaje duplicado de campana ignorado: {}", whatsappMessageId);
            return;
        }

        Mensaje msgEntrante = Mensaje.builder()
                .conversacion(conversacion)
                .tipoRemitente(TipoRemitente.CLIENTE)
                .contenido(textoRecibido != null ? textoRecibido : "[Contenido multimedia]")
                .whatsappMessageId(whatsappMessageId)
                .build();
        mensajeRepository.save(msgEntrante);

        conversacion.setTotalMensajes(conversacion.getTotalMensajes() + 1);
        conversacion.setFechaUltimoMensaje(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        // 3. Generar respuesta con IA usando info de la campana
        String respuestaIA = generarRespuestaCampana(campana, conversacion, textoRecibido);

        if (respuestaIA == null || respuestaIA.isBlank()) {
            log.warn("No se pudo generar respuesta para contacto de campana {}", telefono);
            return;
        }

        // 4. Detectar si la IA quiere enviar el link del grupo
        boolean enviarLink = respuestaIA.contains(TOKEN_ENVIAR_LINK);
        String mensajeLimpio = respuestaIA.replace(TOKEN_ENVIAR_LINK, "").trim();

        // 5. Enviar respuesta
        SendMessageResponse response = whatsAppService.enviarMensaje(
                empresa.getId(), telefono, mensajeLimpio);

        if (response != null && response.isSuccess()) {
            Mensaje msgBot = Mensaje.builder()
                    .conversacion(conversacion)
                    .tipoRemitente(TipoRemitente.BOT)
                    .contenido(mensajeLimpio)
                    .whatsappMessageId(response.getMessageId())
                    .build();
            mensajeRepository.save(msgBot);

            conversacion.setTotalMensajes(conversacion.getTotalMensajes() + 1);
            conversacion.setFechaUltimoMensaje(LocalDateTime.now());
            conversacionRepository.save(conversacion);

            log.info("Campana {}: respuesta enviada a {}", campana.getId(), telefono);
        }

        // 6. Si corresponde, enviar link del grupo como mensaje separado
        if (enviarLink && campana.getLinkGrupoInvitacion() != null) {
            try {
                // Pequeno delay para que parezca natural
                Thread.sleep(3000);

                String msgLink = "Aqui tienes el link para unirte \uD83D\uDC47\n" + campana.getLinkGrupoInvitacion();
                SendMessageResponse linkResponse = whatsAppService.enviarMensaje(
                        empresa.getId(), telefono, msgLink);

                if (linkResponse != null && linkResponse.isSuccess()) {
                    Mensaje msgLinkBot = Mensaje.builder()
                            .conversacion(conversacion)
                            .tipoRemitente(TipoRemitente.BOT)
                            .contenido(msgLink)
                            .whatsappMessageId(linkResponse.getMessageId())
                            .build();
                    mensajeRepository.save(msgLinkBot);

                    log.info("Campana {}: link de grupo enviado a {}", campana.getId(), telefono);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Genera respuesta con IA usando el contexto de la campana
     */
    private String generarRespuestaCampana(Campana campana, Conversacion conversacion, String mensajeCliente) {
        try {
            String apiKey = configuracionSaasService.obtenerOpenAiApiKey();

            String systemPrompt = buildCampanaPrompt(campana);

            // Cargar historial de conversacion (ultimos 15 mensajes)
            Pageable pageable = PageRequest.of(0, 15);
            List<Mensaje> historial = mensajeRepository.findUltimosMensajes(
                    conversacion.getId(), pageable);

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.system(systemPrompt));

            // Agregar historial en orden cronologico
            List<Mensaje> historialOrdenado = new ArrayList<>(historial);
            Collections.reverse(historialOrdenado);
            for (Mensaje msg : historialOrdenado) {
                if (msg.getTipoRemitente() == TipoRemitente.CLIENTE) {
                    messages.add(ChatMessage.user(msg.getContenido()));
                } else if (msg.getTipoRemitente() == TipoRemitente.BOT) {
                    messages.add(ChatMessage.assistant(msg.getContenido()));
                }
            }

            // Agregar mensaje actual
            messages.add(ChatMessage.user(mensajeCliente));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(messages)
                    .maxTokens(500)
                    .temperature(0.8)
                    .build();

            ChatCompletionResponse response = callOpenAI(apiKey, request);

            if (response != null && response.getResponseText() != null) {
                return response.getResponseText();
            }

            return null;

        } catch (Exception e) {
            log.error("Error generando respuesta de campana: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Construye el prompt del sistema para conversaciones de campana.
     * Combina: personalidad natural + info del negocio + tecnicas de venta + reglas
     */
    private String buildCampanaPrompt(Campana campana) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                Eres un vendedor REAL de perfumes que habla por WhatsApp. NO eres un bot.
                Hablas como una persona normal peruana, amigable y directa.
                
                PERSONALIDAD:
                - Hablas en espanol peruano natural (puedes usar "pe", "ya", "bacan", "causa")
                - Mensajes CORTOS como WhatsApp real (2-4 lineas maximo)
                - Usas emojis con moderacion (1-2 por mensaje)
                - NO uses formato formal, ni "estimado", ni "cordial saludo"
                - NO uses listas con bullets ni formato markdown
                - Responde como si escribieras desde tu celular a un amigo
                - Si te preguntan si eres bot, di que no, que eres [nombre del negocio]
                
                TECNICAS DE VENTA (usarlas de manera natural, NO forzada):
                - Escucha activa: repite lo que el cliente dice para mostrar que entiendes
                - Urgencia suave: "justo me quedan pocos" o "esta oferta es solo esta semana"
                - Prueba social: "varios del grupo ya me compraron" o "es el mas pedido"
                - Objeciones de precio: ofrece opciones, destaca calidad, compara con originales
                - Cierre suave: no presiones, guia naturalmente hacia la compra o hacia unirse al grupo
                
                """);

        // Info del negocio
        if (campana.getInfoNegocio() != null && !campana.getInfoNegocio().isBlank()) {
            prompt.append("INFORMACION DE MI NEGOCIO:\n")
                  .append(campana.getInfoNegocio()).append("\n\n");
        }

        // Precios
        if (campana.getLinkPrecios() != null && !campana.getLinkPrecios().isBlank()) {
            prompt.append("LINK DE PRECIOS/CATALOGO:\n")
                  .append("Si el cliente pide precios o catalogo, comparte este link: ")
                  .append(campana.getLinkPrecios()).append("\n\n");
        }

        // Beneficios del grupo
        if (campana.getBeneficiosGrupo() != null && !campana.getBeneficiosGrupo().isBlank()) {
            prompt.append("BENEFICIOS DE UNIRSE A NUESTRO GRUPO:\n")
                  .append(campana.getBeneficiosGrupo()).append("\n\n");
        }

        // Instrucciones adicionales del usuario
        if (campana.getPromptMensaje() != null && !campana.getPromptMensaje().isBlank()) {
            prompt.append("INSTRUCCIONES ADICIONALES:\n")
                  .append(campana.getPromptMensaje()).append("\n\n");
        }

        // Regla del link de grupo
        if (campana.getLinkGrupoInvitacion() != null && !campana.getLinkGrupoInvitacion().isBlank()) {
            prompt.append("""
                    REGLA IMPORTANTE SOBRE EL LINK DEL GRUPO:
                    - Cuando la persona diga que SI quiere unirse, que le interesa el grupo, 
                      o que quiere ser parte, agrega EXACTAMENTE el texto [ENVIAR_LINK] al final de tu respuesta.
                    - Solo agrega [ENVIAR_LINK] cuando la persona CONFIRME que quiere unirse.
                    - NO lo agregues si solo esta preguntando o dudando.
                    - El link se enviara automaticamente en un mensaje separado.
                    - NO escribas tu el link, solo agrega [ENVIAR_LINK] y el sistema lo envia.
                    """);
        }

        return prompt.toString();
    }

    private Conversacion findOrCreateConversacionCampana(Empresa empresa, String telefono, String nombre) {
        return conversacionRepository.findConversacionActiva(empresa.getId(), telefono)
                .orElseGet(() -> {
                    Conversacion nueva = Conversacion.builder()
                            .empresa(empresa)
                            .telefonoCliente(telefono)
                            .nombreCliente(nombre)
                            .estado(EstadoConversacion.ACTIVA)
                            .totalMensajes(0)
                            .fechaUltimoMensaje(LocalDateTime.now())
                            .build();
                    log.info("Nueva conversacion de campana para: {}", telefono);
                    return conversacionRepository.save(nueva);
                });
    }

    private ChatCompletionResponse callOpenAI(String apiKey, ChatCompletionRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<ChatCompletionRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(
                    OPENAI_API_URL, HttpMethod.POST, entity, ChatCompletionResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            log.error("Error llamando a OpenAI: {}", e.getMessage());
            return null;
        }
    }
}
