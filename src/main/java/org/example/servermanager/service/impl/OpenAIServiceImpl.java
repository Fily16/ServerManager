package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.openai.ChatCompletionRequest;
import org.example.servermanager.dto.openai.ChatCompletionResponse;
import org.example.servermanager.dto.openai.ChatMessage;
import org.example.servermanager.entity.*;
import org.example.servermanager.enums.TipoRemitente;
import org.example.servermanager.repository.ArchivoCatalogoRepository;
import org.example.servermanager.repository.MensajeRepository;
import org.example.servermanager.repository.TecnicaVentaRepository;
import org.example.servermanager.service.ConfiguracionSaasService;
import org.example.servermanager.service.OpenAIService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIServiceImpl implements OpenAIService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final int MAX_HISTORY_MESSAGES = 10;

    private final ConfiguracionSaasService configuracionSaasService;
    private final TecnicaVentaRepository tecnicaVentaRepository;
    private final MensajeRepository mensajeRepository;
    private final ArchivoCatalogoRepository archivoCatalogoRepository;
    private final RestTemplate restTemplate;

    @Override
    public OpenAIResponse generarRespuestaVenta(
            ConfiguracionBot config,
            Conversacion conversacion,
            String mensajeCliente,
            List<Producto> productos) {

        try {
            // 1. Construir el prompt del sistema
            String systemPrompt = buildSystemPrompt(config, productos);

            // 2. Construir lista de mensajes (historial + nuevo)
            List<ChatMessage> messages = buildMessageList(systemPrompt, conversacion, mensajeCliente);

            // 3. Obtener modelo y API key
            String model = config.getModeloAi() != null ? config.getModeloAi() : DEFAULT_MODEL;
            String apiKey = getOpenAIApiKey();

            // 4. Crear request
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(300)
                    .temperature(0.9)
                    .presencePenalty(0.6)
                    .frequencyPenalty(0.4)
                    .build();

            // 5. Llamar a OpenAI
            ChatCompletionResponse response = callOpenAI(apiKey, request);

            if (response != null && response.getResponseText() != null) {
                return new OpenAIResponse(
                        response.getResponseText(),
                        response.getTotalTokens()
                );
            }

            return new OpenAIResponse("hola! que necesitas?", 0);

        } catch (Exception e) {
            log.error("Error generando respuesta con OpenAI: {}", e.getMessage(), e);
            return new OpenAIResponse(
                    "hola! como te puedo ayudar?",
                    0
            );
        }
    }

    /**
     * Construye el System Prompt completo.
     * Si el usuario configuro un promptSistema en el dashboard, se usa como base principal.
     * Si no, se usa el prompt maestro de ventas por defecto.
     * Siempre se agrega info de la empresa y catalogo.
     */
    private String buildSystemPrompt(ConfiguracionBot config, List<Producto> productos) {
        StringBuilder prompt = new StringBuilder();

        // ==================== PROMPT BASE (configurable desde dashboard o default) ====================
        if (config.getPromptSistema() != null && !config.getPromptSistema().isBlank()) {
            // Usar el prompt personalizado del dashboard
            prompt.append(config.getPromptSistema());
        } else {
            // Usar el prompt maestro de ventas por defecto
            prompt.append(getMasterSalesPrompt());

            prompt.append("\n\n## COMO ESCRIBES (LEE ESTO ES LO MAS IMPORTANTE)\n");
            prompt.append("""
                Escribes EXACTAMENTE como una chica real de 24 años escribe por WhatsApp. NO como un bot. NO como servicio al cliente. Como una PERSONA REAL.

                Asi escribes tu:
                - todo en minusculas, casi nunca usas mayusculas
                - no pones punto al final de los mensajes
                - a veces no pones tildes: "esta" en vez de "está", "tambien" en vez de "también"
                - mensajes SUPER cortos, 1 linea o maximo 2
                - JAMAS uses negritas, asteriscos, listas, viñetas, numeracion ni nada de formato
                - a veces solo respondes con una palabra: "sii", "claro", "dale"
                - usas "jaja" o "jajaj" naturalmente
                - dices cosas como "mira", "oye", "es que", "a ver", "bueno"
                - para pensar usas "mmm" o "a ver dejame ver..."
                - a veces pones emojis pero no siempre, y maximo 1 por mensaje
                - NUNCA uses "!" al inicio como "¡Hola!" - una persona real escribe "hola" o "holaa"

                Ejemplos de como hablas tu:
                "holaa, que buscas?"
                "sii ese lo tenemos, esta a 320"
                "mmm dejame ver... si creo q si"
                "te muestro el catalogo?"
                "ese esta buenisimo la verdad"
                "claro, te lo mando"

                NUNCA digas estas cosas (son de bot):
                "¡Hola! ¿En qué puedo ayudarte?"
                "Con gusto te ayudo"
                "¿Hay algo más en que pueda asistirte?"
                "Excelente elección"
                "Quedamos a tu disposición"
                cualquier frase que suene a servicio al cliente

                NUNCA inventes precios ni info que no tengas
                Si no sabes algo: "uy eso no lo se, dejame preguntar"
                Si preguntan precio dalo directo sin rodeos

                NOTAS DE VOZ: A veces puedes enviar tu respuesta como nota de voz en vez de texto.
                Si quieres que tu respuesta se envie como audio, agrega [VOZ] al final del mensaje.
                Usa [VOZ] cuando:
                - Te presentes por primera vez
                - Expliques algo largo o detallado
                - Quieras sonar mas cercana y personal
                No lo uses siempre, solo cuando tenga sentido. El sistema decide automaticamente a veces tambien.
                """);
        }

        // ==================== INFO DE LA EMPRESA (siempre se agrega) ====================
        prompt.append("\n\n## INFORMACIÓN DE LA EMPRESA\n");
        prompt.append("Nombre del negocio: ").append(config.getEmpresa().getNombre()).append("\n");
        if (config.getNombreBot() != null) {
            prompt.append("Tu nombre es: ").append(config.getNombreBot()).append("\n");
        }

        if (config.getTiempoEntrega() != null && !config.getTiempoEntrega().isEmpty()) {
            prompt.append("Tiempo de entrega: ").append(config.getTiempoEntrega()).append("\n");
        }

        if (config.getLinkCatalogo() != null && !config.getLinkCatalogo().isEmpty()) {
            prompt.append("Link del catalogo de precios: ").append(config.getLinkCatalogo()).append("\n");
            prompt.append("Si te piden el catalogo o precios generales, comparte este link\n");
        }

        if (config.getLinkGrupoConsolidado() != null && !config.getLinkGrupoConsolidado().isEmpty()) {
            prompt.append("Link del grupo de WhatsApp: ").append(config.getLinkGrupoConsolidado()).append("\n");
            prompt.append("Si la persona quiere unirse o pide el link del grupo, compartelo\n");
        }

        if (config.getLinkTiktok() != null && !config.getLinkTiktok().isEmpty()) {
            prompt.append("TikTok/Redes sociales: ").append(config.getLinkTiktok()).append("\n");
        }

        // ==================== CATÁLOGO DE PRODUCTOS ====================
        if (productos != null && !productos.isEmpty()) {
            prompt.append("\n## CATÁLOGO DE PRODUCTOS DISPONIBLES\n");
            prompt.append("Estos son los productos que puedes ofrecer y vender:\n\n");

            for (Producto p : productos) {
                prompt.append("- **").append(p.getNombre()).append("**");
                prompt.append(" | Precio: S/").append(p.getPrecio());

                if (p.getPrecioOferta() != null) {
                    prompt.append(" (OFERTA: S/").append(p.getPrecioOferta()).append(")");
                }

                if (p.getDescripcion() != null && !p.getDescripcion().isEmpty()) {
                    String desc = p.getDescripcion();
                    if (desc.length() > 100) {
                        desc = desc.substring(0, 100) + "...";
                    }
                    prompt.append(" - ").append(desc);
                }

                if (p.getTieneStock() && p.getStockActual() != null && p.getStockActual() <= 5) {
                    prompt.append(" ⚠️ ¡ÚLTIMAS UNIDADES!");
                }

                prompt.append("\n");
            }
        }

        // ==================== MÉTODOS DE PAGO ====================
        prompt.append("\n## MÉTODOS DE PAGO\n");
        prompt.append("- Yape\n");
        prompt.append("- Plin\n");
        prompt.append("- Transferencia bancaria\n");
        prompt.append("- Efectivo contra entrega (si aplica)\n");

        // ==================== CATÁLOGO COMPLETO (PDFs/Excel/Sheets procesados) ====================
        try {
            List<ArchivoCatalogo> catalogos = archivoCatalogoRepository.findByEmpresaIdAndActivoTrue(
                    config.getEmpresa().getId());

            if (!catalogos.isEmpty()) {
                prompt.append("\n## TU CATALOGO (esta es la info de tus productos, usala para responder)\n\n");

                for (ArchivoCatalogo cat : catalogos) {
                    log.info("Catalogo cargado: id={}, nombre={}, paginas={}, texto={}chars",
                            cat.getId(), cat.getNombreOriginal(), cat.getTotalPaginas(),
                            cat.getTextoExtraido() != null ? cat.getTextoExtraido().length() : 0);

                    if (cat.getTextoExtraido() != null && !cat.getTextoExtraido().isBlank()) {
                        prompt.append(cat.getTextoExtraido()).append("\n\n");
                    }

                    if (cat.getTotalPaginas() > 0) {
                        prompt.append("ENVIAR FOTOS DEL CATALOGO: Cuando el cliente pregunte por un perfume, te pida que le muestres, ");
                        prompt.append("o quiera ver algo, SIEMPRE incluye el marcador [PAGINA:").append(cat.getId()).append(":X] ");
                        prompt.append("donde X es el numero de pagina donde aparece ese perfume (1 a ").append(cat.getTotalPaginas()).append("). ");
                        prompt.append("El sistema le enviara la foto automaticamente al cliente. ");
                        prompt.append("EJEMPLO: si el cliente dice 'tienes khamrah?' tu respondes 'sii mira te muestro [PAGINA:").append(cat.getId()).append(":3]' ");
                        prompt.append("(cambia el 3 por la pagina real donde aparece). ");
                        prompt.append("USA ESTO SIEMPRE que hablen de un perfume especifico.\n\n");
                    }
                }
            } else {
                log.warn("No hay catalogos activos para empresa {}", config.getEmpresa().getId());
            }
        } catch (Exception e) {
            log.warn("Error cargando catálogos para prompt: {}", e.getMessage());
        }

        return prompt.toString();
    }

    /**
     * PROMPT MAESTRO DE VENTAS
     * Basado en técnicas de:
     * - $100M Offers (Alex Hormozi)
     * - Way of the Wolf (Jordan Belfort)
     * - SPIN Selling
     * - Never Split the Difference (Chris Voss)
     * - The Challenger Sale
     */
    private String getMasterSalesPrompt() {
        // Primero intentamos cargar técnicas de BD
        List<TecnicaVenta> tecnicas = tecnicaVentaRepository.findAllActivas();
        
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("""
            Eres una chica real que vende perfumes por WhatsApp. Te encantan los perfumes y sabes mucho del tema. Hablas como cualquier persona normal por chat, nada formal.

            Tu objetivo es ayudar al cliente a encontrar lo que busca y cerrar la venta de forma natural, como haria cualquier vendedora real.

            Si te preguntan por un perfume especifico, usa la info del catalogo que tienes abajo.
            Si te piden que les muestres, envia la pagina del catalogo donde aparece ese perfume.
            Si dicen que esta caro, ofrece alternativas mas baratas.
            Si dicen "lo pienso", pregunta que duda tienen.
            Si preguntan precio, dalo directo sin rodeos.

            JAMAS digas que eres IA, bot o asistente.
            NUNCA inventes precios o datos.
            Haz una pregunta a la vez, no bombardees.
            """);

        // Si hay técnicas adicionales en BD, agregarlas
        if (!tecnicas.isEmpty()) {
            prompt.append("\n\n## TÉCNICAS ADICIONALES DE VENTA\n");
            for (TecnicaVenta t : tecnicas) {
                prompt.append("\n### ").append(t.getNombre()).append("\n");
                prompt.append(t.getDescripcion()).append("\n");
                if (t.getEjemplo() != null) {
                    prompt.append("Ejemplo: ").append(t.getEjemplo()).append("\n");
                }
            }
        }

        return prompt.toString();
    }

    /**
     * Construye la lista de mensajes incluyendo historial
     */
    private List<ChatMessage> buildMessageList(String systemPrompt, Conversacion conversacion, String nuevoMensaje) {
        List<ChatMessage> messages = new ArrayList<>();

        // 1. System prompt
        messages.add(ChatMessage.system(systemPrompt));

        // 2. Historial de la conversación (últimos N mensajes)
        if (conversacion != null && conversacion.getId() != null) {
            List<Mensaje> historial = mensajeRepository.findUltimosMensajes(
                    conversacion.getId(),
                    PageRequest.of(0, MAX_HISTORY_MESSAGES)
            );

            // Invertir para orden cronológico
            for (int i = historial.size() - 1; i >= 0; i--) {
                Mensaje m = historial.get(i);
                if (m.getTipoRemitente() == TipoRemitente.CLIENTE) {
                    messages.add(ChatMessage.user(m.getContenido()));
                } else {
                    messages.add(ChatMessage.assistant(m.getContenido()));
                }
            }
        }

        // 3. Nuevo mensaje del cliente
        messages.add(ChatMessage.user(nuevoMensaje));

        return messages;
    }

    @Override
    public boolean esConsultaSobrePerfumes(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) return false;

        try {
            String systemPrompt = """
                Eres un clasificador. Determina si el mensaje del usuario está relacionado con:
                perfumes, fragancias, aromas, colonias, productos de belleza, catálogo de perfumes,
                precios de perfumes, consultas de compra de perfumes, o cualquier referencia a
                nombres de perfumes (como "9 AM", "Mademoiselle", etc.).

                Responde UNICAMENTE con "SI" o "NO". Nada más.""";

            List<ChatMessage> messages = List.of(
                    ChatMessage.system(systemPrompt),
                    ChatMessage.user(mensaje)
            );

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(DEFAULT_MODEL)
                    .messages(messages)
                    .maxTokens(3)
                    .temperature(0.0)
                    .build();

            String apiKey = getOpenAIApiKey();
            ChatCompletionResponse response = callOpenAI(apiKey, request);

            if (response != null && response.getResponseText() != null) {
                String respuesta = response.getResponseText().trim().toUpperCase();
                log.info("Clasificación perfume para '{}': {}", mensaje.substring(0, Math.min(mensaje.length(), 40)), respuesta);
                return respuesta.contains("SI") || respuesta.contains("SÍ");
            }

            // Si falla la clasificación, dejar pasar (mejor responder de más que ignorar un cliente)
            return true;
        } catch (Exception e) {
            log.warn("Error clasificando mensaje, dejando pasar: {}", e.getMessage());
            return true; // En caso de error, responder por seguridad
        }
    }

    /**
     * Obtiene la API Key de OpenAI desde configuración global
     */
    private String getOpenAIApiKey() {
        return configuracionSaasService.obtenerOpenAiApiKey();
    }

    /**
     * Llama a la API de OpenAI
     */
    private ChatCompletionResponse callOpenAI(String apiKey, ChatCompletionRequest request) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("sk-tu-api-key-aqui")) {
            log.error("API Key de OpenAI no configurada. Ve a Configuracion en el dashboard y guarda tu API Key.");
            throw new RuntimeException("API Key de OpenAI no configurada");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<ChatCompletionRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Llamando a OpenAI con modelo: {}", request.getModel());

            ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    entity,
                    ChatCompletionResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Respuesta de OpenAI recibida. Tokens usados: {}",
                        response.getBody().getTotalTokens());
                return response.getBody();
            }

            return null;
        } catch (Exception e) {
            log.error("Error llamando a OpenAI: {}", e.getMessage());
            throw e;
        }
    }
}
