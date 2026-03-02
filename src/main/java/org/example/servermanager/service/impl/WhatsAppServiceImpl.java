package org.example.servermanager.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.webhook.*;
import org.example.servermanager.entity.*;
import org.example.servermanager.enums.EstadoConversacion;
import org.example.servermanager.enums.TipoContenido;
import org.example.servermanager.enums.TipoRemitente;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.*;
import org.example.servermanager.service.OpenAIService;
import org.example.servermanager.service.WhatsAppService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    private final ConfiguracionBotRepository configuracionBotRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final ProductoRepository productoRepository;
    private final MensajeCampanaRepository mensajeCampanaRepository;
    private final CampanaRepository campanaRepository;
    private final OpenAIService openAIService;
    private final RestTemplate restTemplate;
    private final CampanaConversacionService campanaConversacionService;
    private final ElevenLabsService elevenLabsService;

    // Buffer: espera a que el usuario termine de escribir antes de responder
    // Clave: JID del usuario, Valor: tarea programada que se cancela si llega otro mensaje
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingResponses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StringBuilder> messageBuffer = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final long BUFFER_DELAY_MS = 10000; // esperar 10 segundos sin actividad

    public WhatsAppServiceImpl(
            ConfiguracionBotRepository configuracionBotRepository,
            EmpresaRepository empresaRepository,
            ClienteRepository clienteRepository,
            ConversacionRepository conversacionRepository,
            MensajeRepository mensajeRepository,
            ProductoRepository productoRepository,
            MensajeCampanaRepository mensajeCampanaRepository,
            CampanaRepository campanaRepository,
            OpenAIService openAIService,
            RestTemplate restTemplate,
            @Lazy CampanaConversacionService campanaConversacionService,
            ElevenLabsService elevenLabsService) {
        this.configuracionBotRepository = configuracionBotRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.productoRepository = productoRepository;
        this.mensajeCampanaRepository = mensajeCampanaRepository;
        this.campanaRepository = campanaRepository;
        this.openAIService = openAIService;
        this.restTemplate = restTemplate;
        this.campanaConversacionService = campanaConversacionService;
        this.elevenLabsService = elevenLabsService;
    }

    @Override
    @Async("webhookExecutor")
    @Transactional
    public void processWebhookEvent(WebhookEvent event) {
      try {
        log.info("Procesando webhook: evento={}, instancia={}", event.getEvent(), event.getInstance());

        // 1. Interceptar historial y NOMBRES DE CONTACTOS (contacts.set, contacts.upsert, etc.)
        if (event.isSyncEvent() || event.getEvent().contains("set") || event.getEvent().contains("chats.") || event.getEvent().contains("contacts.")) {
            log.info("Cargando historial/contactos para AromaStudio: {}", event.getEvent());
            procesarSincronizacionHistorica(event);
            return;
        }

        // 2. MENSAJES EN VIVO (Para que el Dashboard se actualice siempre)
        if (event.isNewMessage()) { // messages.upsert
            MessageData data = event.getData();
            if (data == null) return;

            Optional<ConfiguracionBot> configOpt = configuracionBotRepository.findByEvolutionInstancia(event.getInstance());
            if (configOpt.isEmpty()) return;

            ConfiguracionBot config = configOpt.get();
            Empresa empresa = config.getEmpresa();

            // Buscar o crear carpetas SIEMPRE
            String telefonoLimpio = limpiarTelefono(data.getTelefono());
            Cliente cliente = findOrCreateCliente(empresa, telefonoLimpio, data.getNombre());
            Conversacion conversacion = findOrCreateConversacion(empresa, cliente, telefonoLimpio, data.getNombre());
            // GUARDAR EL MENSAJE EN BASE DE DATOS (Sea tuyo o del cliente)
            if (!data.isIncoming()) {
                // Si lo enviaste tú desde tu celular
                guardarMensajeBot(conversacion, data.getTexto() != null ? data.getTexto() : "[Multimedia]", data.getWhatsAppMessageId());
            } else {
                // Si lo envió el cliente (Ej: tu mamá)
                guardarMensajeEntrante(conversacion, data);
            }

            // 3. LA INTELIGENCIA ARTIFICIAL
            boolean esValido = event.isValidIncomingMessage();
            boolean botActivo = Boolean.TRUE.equals(config.getActivo());
            boolean empresaActiva = Boolean.TRUE.equals(empresa.getActivo());
            boolean autoRespuestaActiva = Boolean.TRUE.equals(config.getAutoRespuesta());

            log.info("Webhook mensaje de {}: valido={}, botActivo={}, empresaActiva={}, autoRespuesta={}, texto='{}'",
                    data.getTelefono(), esValido, botActivo, empresaActiva, autoRespuestaActiva,
                    data.getTexto() != null ? data.getTexto().substring(0, Math.min(data.getTexto().length(), 50)) : "null");

            if (esValido && botActivo && empresaActiva && autoRespuestaActiva) {

                // Usar el JID original para enviar respuestas (soporta @s.whatsapp.net y @lid)
                String jidParaResponder = data.getKey().getRemoteJid();
                log.info("JID para responder: {}", jidParaResponder);

                // Verificar si es un contacto de campaña
                Optional<MensajeCampana> contactoCampana = mensajeCampanaRepository.findContactoCampanaActiva(empresa.getId(), data.getTelefono());
                if (contactoCampana.isPresent()) {
                    log.info("Mensaje de {} es contacto de campaña, derivando", data.getTelefono());
                    campanaConversacionService.procesarRespuestaCampana(empresa, contactoCampana.get().getCampana(), data.getTelefono(), data.getNombre(), data.getTexto(), data.getWhatsAppMessageId());
                    return;
                }

                // Verificar Horario
                if (!isDentroHorarioAtencion(config)) {
                    log.info("Fuera de horario, enviando mensaje a {}", jidParaResponder);
                    String mensajeFueraHorario = config.getMensajeFueraHorario() != null ? config.getMensajeFueraHorario() : "¡Hola! Estamos fuera de horario. Te responderemos pronto.";
                    enviarMensaje(empresa.getId(), jidParaResponder, mensajeFueraHorario);
                    return;
                }

                // Buffer: acumular mensajes y esperar a que termine de escribir
                String textoMensaje = data.getTexto();
                if (textoMensaje == null || textoMensaje.isBlank()) {
                    // Audio u otro multimedia sin texto - responder que no se pueden procesar audios
                    if ("audioMessage".equalsIgnoreCase(data.getMessageType()) ||
                        "pttMessage".equalsIgnoreCase(data.getMessageType()) ||
                        "ptvaudiomessage".equalsIgnoreCase(data.getMessageType())) {
                        bufferearYResponder(jidParaResponder, "no puedo escuchar audios, me escribes porfa? 🙏",
                                config, empresa, conversacion);
                        return;
                    }
                    log.info("Mensaje sin texto de {}, ignorando", jidParaResponder);
                    return;
                }

                // Acumular en el buffer
                messageBuffer.computeIfAbsent(jidParaResponder, k -> new StringBuilder());
                StringBuilder buffer = messageBuffer.get(jidParaResponder);
                if (buffer.length() > 0) buffer.append("\n");
                buffer.append(textoMensaje);

                // Cancelar la respuesta pendiente si habia una (el usuario sigue escribiendo)
                ScheduledFuture<?> existing = pendingResponses.get(jidParaResponder);
                if (existing != null && !existing.isDone()) {
                    existing.cancel(false);
                    log.info("Buffer: cancelada respuesta pendiente para {}, esperando mas mensajes...", jidParaResponder);
                }

                // Programar respuesta en BUFFER_DELAY_MS si no llegan mas mensajes
                final ConfiguracionBot configFinal = config;
                final Empresa empresaFinal = empresa;
                final Conversacion conversacionFinal = conversacion;
                final String jidFinal = jidParaResponder;

                ScheduledFuture<?> future = scheduler.schedule(() -> {
                    try {
                        String mensajeCompleto = messageBuffer.remove(jidFinal).toString();
                        pendingResponses.remove(jidFinal);
                        log.info("Buffer completo para {}: '{}'", jidFinal,
                                mensajeCompleto.substring(0, Math.min(mensajeCompleto.length(), 80)));

                        String respuesta = generarRespuestaConIA(configFinal, conversacionFinal, mensajeCompleto);
                        if (respuesta != null && !respuesta.isEmpty()) {
                            enviarRespuestaConImagenes(configFinal, empresaFinal, conversacionFinal, jidFinal, respuesta, mensajeCompleto);
                        }
                    } catch (Exception e) {
                        log.error("Error procesando buffer para {}: {}", jidFinal, e.getMessage());
                        messageBuffer.remove(jidFinal);
                        pendingResponses.remove(jidFinal);
                    }
                }, BUFFER_DELAY_MS, TimeUnit.MILLISECONDS);

                pendingResponses.put(jidParaResponder, future);
            } else if (esValido && !autoRespuestaActiva) {
                log.info("Auto-respuesta DESACTIVADA - mensaje de {} guardado pero no respondido", data.getTelefono());
            }
        }
      } catch (Exception e) {
        log.error("ERROR CRITICO procesando webhook: [{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);
      }
    }

    /**
     * Nuevo método para extraer los arrays JSON de historial y poblar el dashboard
     */
    /**
     * Extrae los arrays JSON de historial y puebla el dashboard
     */
    private void procesarSincronizacionHistorica(WebhookEvent event) {
        if (event.getRawData() == null || !event.getRawData().isArray()) {
            return;
        }

        // 1. Obtener la empresa asociada a la instancia para poder crear los registros
        Optional<ConfiguracionBot> configOpt = configuracionBotRepository.findByEvolutionInstancia(event.getInstance());
        if (configOpt.isEmpty()) {
            log.warn("Sincronización ignorada: No se encontró empresa activa para la instancia {}", event.getInstance());
            return;
        }
        Empresa empresa = configOpt.get().getEmpresa();
        String tipoEvento = event.getEvent();

        log.info("Procesando lote de {} registros para el evento {}", event.getRawData().size(), tipoEvento);

        for (JsonNode nodo : event.getRawData()) {
            try {
                // 2. Extraer el identificador de WhatsApp (puede venir como 'id', 'remoteJid' o dentro de 'key')
                String rawJid = null;
                if (nodo.has("id")) {
                    rawJid = nodo.get("id").asText();
                } else if (nodo.has("remoteJid")) {
                    rawJid = nodo.get("remoteJid").asText();
                } else if (nodo.has("key") && nodo.get("key").has("remoteJid")) {
                    rawJid = nodo.get("key").get("remoteJid").asText();
                }

                // 3. Filtrar: Ignorar si es nulo, si es un grupo o si son estados
                if (rawJid == null || rawJid.contains("@g.us") || rawJid.contains("status@broadcast")) {
                    continue;
                }

                // 4. Limpiar el número (quitar el @s.whatsapp.net)
                String telefono = rawJid.split("@")[0];

                // 5. Extraer el nombre (Evolution lo manda en diferentes campos según el evento)
                String nombre = telefono; // Por defecto usamos el teléfono
                if (nodo.has("name") && !nodo.get("name").isNull()) {
                    nombre = nodo.get("name").asText();
                } else if (nodo.has("pushName") && !nodo.get("pushName").isNull()) {
                    nombre = nodo.get("pushName").asText();
                } else if (nodo.has("verifiedName") && !nodo.get("verifiedName").isNull()) {
                    nombre = nodo.get("verifiedName").asText();
                }

                // 6. Solo crear Cliente y Conversacion (para el dashboard)
                // NO guardamos mensajes históricos para ahorrar espacio en BD
                Cliente cliente = findOrCreateCliente(empresa, telefono, nombre);
                findOrCreateConversacion(empresa, cliente, telefono, nombre);

            } catch (Exception e) {
                log.error("Error guardando registro histórico individual: {}", e.getMessage());
            }
        }

        log.info("Sincronización del evento {} finalizada con éxito.", tipoEvento);
    }

    @Override
    public SendMessageResponse enviarMensaje(Long empresaId, String telefono, String mensaje) {
        ConfiguracionBot config = getConfiguracionBot(empresaId);
        return sendToEvolutionApi(config, SendMessageRequest.text(telefono, mensaje));
    }

    @Override
    public SendMessageResponse enviarRespuesta(Long empresaId, String telefono, String mensaje, String messageIdOriginal) {
        ConfiguracionBot config = getConfiguracionBot(empresaId);
        return sendToEvolutionApi(config, SendMessageRequest.reply(telefono, mensaje, messageIdOriginal));
    }

    @Override
    public boolean isInstanceConnected(Long empresaId) {
        try {
            ConfiguracionBot config = getConfiguracionBot(empresaId);
            String url = buildBaileysUrl(config, "/status");

            ResponseEntity<java.util.Map> response = restTemplate.getForEntity(url, java.util.Map.class);
            return response.getStatusCode().is2xxSuccessful()
                    && "open".equals(response.getBody().get("status"));
        } catch (Exception e) {
            log.error("Error verificando conexion WhatsApp: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Genera respuesta usando OpenAI con el prompt maestro de ventas
     */
    private String generarRespuestaConIA(ConfiguracionBot config, Conversacion conversacion, String mensajeCliente) {
        try {
            // Obtener productos de la empresa para el catálogo
            List<Producto> productos = productoRepository.findByEmpresaIdAndActivoTrue(
                    config.getEmpresa().getId()
            );

            // Llamar al servicio de OpenAI
            OpenAIService.OpenAIResponse respuesta = openAIService.generarRespuestaVenta(
                    config,
                    conversacion,
                    mensajeCliente,
                    productos
            );

            log.info("Respuesta generada con {} tokens", respuesta.tokensUsados());
            return respuesta.texto();

        } catch (Exception e) {
            log.error("Error generando respuesta con IA: {}", e.getMessage());
            
            // Fallback: mensaje de bienvenida o genérico
            if (conversacion.getTotalMensajes() <= 1 && config.getMensajeBienvenida() != null) {
                return config.getMensajeBienvenida();
            }
            return "hola! como te puedo ayudar?";
        }
    }

    /**
     * Verifica si estamos dentro del horario de atención configurado
     */
    private boolean isDentroHorarioAtencion(ConfiguracionBot config) {
        if (config.getHorarioInicio() == null || config.getHorarioFin() == null) {
            // Si no hay horario configurado, siempre disponible
            return true;
        }

        LocalTime ahora = LocalTime.now();
        LocalTime inicio = config.getHorarioInicio();
        LocalTime fin = config.getHorarioFin();

        // Manejo de horarios que cruzan medianoche (ej: 20:00 - 02:00)
        if (fin.isBefore(inicio)) {
            return ahora.isAfter(inicio) || ahora.isBefore(fin);
        }

        return !ahora.isBefore(inicio) && !ahora.isAfter(fin);
    }

    private Cliente findOrCreateCliente(Empresa empresa, String telefonoRaw, String nombreRaw) {
        String telefono = limpiarTelefono(telefonoRaw);
        String nombre = (nombreRaw == null || nombreRaw.isEmpty()) ? telefono : nombreRaw;

        return clienteRepository.findByEmpresaIdAndTelefono(empresa.getId(), telefono)
                .map(cliente -> {
                    // SOLO actualiza el nombre si el nuevo NO es un teléfono y es diferente
                    if (!nombre.equals(telefono) && !nombre.equals(cliente.getNombre())) {
                        cliente.setNombre(nombre);
                        clienteRepository.saveAndFlush(cliente); // Obliga a guardar al instante
                    }
                    return cliente;
                })
                .orElseGet(() -> {
                    Cliente nuevo = Cliente.builder()
                            .empresa(empresa)
                            .telefono(telefono)
                            .nombre(nombre)
                            .totalConversaciones(0)
                            .totalPedidos(0)
                            .build();
                    return clienteRepository.save(nuevo); // Guarda al instante
                });
    }

    private Conversacion findOrCreateConversacion(Empresa empresa, Cliente cliente, String telefonoRaw, String nombreRaw) {
        String telefono = limpiarTelefono(telefonoRaw);
        String nombre = (nombreRaw == null || nombreRaw.isEmpty()) ? telefono : nombreRaw;

        return conversacionRepository.findConversacionActiva(empresa.getId(), telefono)
                .map(conv -> {
                    // Protege el nombre real
                    if (!nombre.equals(telefono) && !nombre.equals(conv.getNombreCliente())) {
                        conv.setNombreCliente(nombre);
                        conversacionRepository.saveAndFlush(conv);
                    }
                    return conv;
                })
                .orElseGet(() -> {
                    cliente.setTotalConversaciones(cliente.getTotalConversaciones() + 1);
                    clienteRepository.saveAndFlush(cliente);

                    Conversacion nueva = Conversacion.builder()
                            .empresa(empresa)
                            .cliente(cliente)
                            .telefonoCliente(telefono)
                            .nombreCliente(nombre)
                            .estado(EstadoConversacion.ACTIVA)
                            .totalMensajes(0)
                            .fechaUltimoMensaje(LocalDateTime.now())
                            .build();
                    log.info("Nueva conversación creada para: {}", telefono);
                    return conversacionRepository.save(nueva);
                });
    }

    private Mensaje guardarMensajeEntrante(Conversacion conversacion, MessageData data) {
        // Verificar si el mensaje ya existe (evitar duplicados)
        if (data.getWhatsAppMessageId() != null && 
            mensajeRepository.existsByWhatsappMessageId(data.getWhatsAppMessageId())) {
            log.debug("Mensaje duplicado ignorado: {}", data.getWhatsAppMessageId());
            return null;
        }

        Mensaje mensaje = Mensaje.builder()
                .conversacion(conversacion)
                .tipoRemitente(TipoRemitente.CLIENTE)
                .contenido(data.getTexto() != null ? data.getTexto() : "[Contenido multimedia]")
                .tipoContenido(data.getTipoContenido())
                .mediaUrl(data.getMediaUrl())
                .whatsappMessageId(data.getWhatsAppMessageId())
                .build();

        conversacion.setTotalMensajes(conversacion.getTotalMensajes() + 1);
        conversacion.setFechaUltimoMensaje(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        return mensajeRepository.save(mensaje);
    }

    private Mensaje guardarMensajeBot(Conversacion conversacion, String texto, String whatsappMessageId) {
        Mensaje mensaje = Mensaje.builder()
                .conversacion(conversacion)
                .tipoRemitente(TipoRemitente.BOT)
                .contenido(texto)
                .whatsappMessageId(whatsappMessageId)
                .build();

        conversacion.setTotalMensajes(conversacion.getTotalMensajes() + 1);
        conversacion.setFechaUltimoMensaje(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        return mensajeRepository.save(mensaje);
    }

    private ConfiguracionBot getConfiguracionBot(Long empresaId) {
        return configuracionBotRepository.findByEmpresaId(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("ConfiguracionBot", "empresaId", empresaId));
    }

    private String buildBaileysUrl(ConfiguracionBot config, String endpoint) {
        String baseUrl = config.getEvolutionApiUrl();

        // 1. FOOLPROOF: Si la base de datos devuelve null, vacío, o algo sin "http", forzamos la URL correcta.
        if (baseUrl == null || !baseUrl.startsWith("http")) {
            log.warn("ATENCION: La URL en la BD era invalida ('{}'). Forzando la URL real de Render.", baseUrl);
            baseUrl = "https://whatsapp-baileys-bot.onrender.com/api";
        }

        // 2. Limpiar espacios o barras accidentales al final
        baseUrl = baseUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 3. Construir la URL final
        String finalUrl = baseUrl + "/" + config.getEvolutionInstancia() + endpoint;

        // 4. Imprimir la URL exacta en los logs para monitorearla
        log.info("URL construida para Baileys: {}", finalUrl);

        return finalUrl;
    }

    private SendMessageResponse sendToEvolutionApi(ConfiguracionBot config, SendMessageRequest request) {
        try {
            String url = buildBaileysUrl(config, "/send-message");

            // Convertir numero a JID para Baileys
            String jid = request.getNumber();
            if (!jid.contains("@")) {
                jid = jid + "@s.whatsapp.net";
            }

            java.util.Map<String, String> body = java.util.Map.of(
                    "jid", jid,
                    "text", request.getText()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.debug("Enviando mensaje a WhatsApp: {} -> {}", request.getNumber(),
                    request.getText().substring(0, Math.min(request.getText().length(), 50)));

            ResponseEntity<java.util.Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, java.util.Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Mensaje enviado exitosamente a {}", request.getNumber());
                Object msgId = response.getBody().get("messageId");
                return SendMessageResponse.builder()
                        .key(MessageKey.builder()
                                .id(msgId != null ? msgId.toString() : "sent")
                                .remoteJid(jid)
                                .fromMe(true)
                                .build())
                        .build();
            } else {
                log.error("Error enviando mensaje: status={}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error enviando mensaje a WhatsApp: {}", e.getMessage());
            return null;
        }
    }
    private String limpiarTelefono(String telefono) {
        if (telefono == null) return "";
        if (telefono.contains("@")) {
            telefono = telefono.split("@")[0];
        }
        return telefono.replace("+", "").replace(" ", "").trim();
    }

    /**
     * Envia una respuesta directa sin pasar por la IA (para audios, etc)
     */
    private void bufferearYResponder(String jid, String texto, ConfiguracionBot config,
                                      Empresa empresa, Conversacion conversacion) {
        SendMessageResponse response = enviarMensaje(empresa.getId(), jid, texto);
        if (response != null && response.isSuccess()) {
            guardarMensajeBot(conversacion, texto, response.getMessageId());
        }
    }

    // Patrón: [PAGINA:catalogoId:numeroPagina]
    private static final Pattern PAGINA_PATTERN = Pattern.compile("\\[PAGINA:(\\d+):(\\d+)\\]");
    // Patrón: [VOZ] marcador que la IA puede poner para forzar nota de voz
    private static final Pattern VOZ_PATTERN = Pattern.compile("\\[VOZ\\]");

    /**
     * Envía la respuesta de IA, detectando marcadores [PAGINA:catId:num] para enviar imágenes
     * y decidiendo si enviar como nota de voz o texto.
     */
    private void enviarRespuestaConImagenes(ConfiguracionBot config, Empresa empresa,
                                             Conversacion conversacion, String jid, String respuesta, String mensajeCliente) {
        try {
            Matcher matcher = PAGINA_PATTERN.matcher(respuesta);

            // Enviar imágenes encontradas en la respuesta
            while (matcher.find()) {
                Long catalogoId = Long.parseLong(matcher.group(1));
                int pagina = Integer.parseInt(matcher.group(2));

                String imageUrl = "http://localhost:8085/api/v1/empresas/" + empresa.getId()
                        + "/archivos/catalogo/" + catalogoId + "/pagina/" + pagina;

                log.info("Enviando imagen de catálogo {}, página {} a {}", catalogoId, pagina, jid);
                enviarImagen(config, jid, imageUrl, "Página " + pagina + " del catálogo");
            }

            // Limpiar marcadores del texto
            String textoLimpio = PAGINA_PATTERN.matcher(respuesta).replaceAll("").trim();
            boolean tieneVozMarker = VOZ_PATTERN.matcher(textoLimpio).find();
            textoLimpio = VOZ_PATTERN.matcher(textoLimpio).replaceAll("").trim();

            if (textoLimpio.isEmpty()) return;

            // Decidir: enviar como voz o como texto
            boolean enviarComoVoz = debeEnviarComoVoz(textoLimpio, conversacion, tieneVozMarker, mensajeCliente);

            if (enviarComoVoz) {
                log.info("Enviando respuesta como NOTA DE VOZ a {} ({} chars)", jid, textoLimpio.length());
                boolean audioEnviado = enviarAudio(config, jid, textoLimpio);

                if (audioEnviado) {
                    guardarMensajeBot(conversacion, "[Nota de voz] " + textoLimpio, null);
                    log.info("Nota de voz enviada a {}", jid);
                } else {
                    // Fallback: enviar como texto si falla el audio
                    log.warn("Fallo la nota de voz, enviando como texto a {}", jid);
                    SendMessageResponse response = enviarMensaje(empresa.getId(), jid, textoLimpio);
                    if (response != null && response.isSuccess()) {
                        guardarMensajeBot(conversacion, textoLimpio, response.getMessageId());
                    }
                }
            } else {
                SendMessageResponse response = enviarMensaje(empresa.getId(), jid, textoLimpio);
                if (response != null && response.isSuccess()) {
                    guardarMensajeBot(conversacion, textoLimpio, response.getMessageId());
                    log.info("Respuesta IA enviada a {}", jid);
                } else {
                    log.warn("Fallo al enviar respuesta a {}", jid);
                }
            }
        } catch (Exception e) {
            log.error("Error enviando respuesta con imágenes: {}", e.getMessage());
            // Fallback: enviar solo texto sin marcadores
            String textoLimpio = PAGINA_PATTERN.matcher(respuesta).replaceAll("").trim();
            textoLimpio = VOZ_PATTERN.matcher(textoLimpio).replaceAll("").trim();
            if (!textoLimpio.isEmpty()) {
                SendMessageResponse response = enviarMensaje(empresa.getId(), jid, textoLimpio);
                if (response != null && response.isSuccess()) {
                    guardarMensajeBot(conversacion, textoLimpio, response.getMessageId());
                }
            }
        }
    }

    // Palabras clave del cliente que activan respuesta por voz (usar raíces para cubrir conjugaciones)
    private static final Pattern KEYWORDS_VOZ = Pattern.compile(
            "recomiend|recomendaci|explic|cu[eé]nt|dime sobre|h[aá]blam|sugier|que perfume|cu[aá]l perfume|present[aá]t|qui[eé]n eres",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Decide si la respuesta se debe enviar como nota de voz.
     */
    private boolean debeEnviarComoVoz(String texto, Conversacion conversacion, boolean tieneVozMarker, String mensajeCliente) {
        // Si ElevenLabs no esta configurado, nunca voz
        if (!elevenLabsService.isConfigured()) {
            log.info("VOZ: ElevenLabs no configurado, enviando como texto");
            return false;
        }

        // Si la IA lo pidió explícitamente
        if (tieneVozMarker) {
            log.info("VOZ: marcador [VOZ] detectado, enviando como audio");
            return true;
        }

        // Primera interaccion (presentación)
        if (conversacion.getTotalMensajes() <= 2) {
            log.info("VOZ: primera interaccion (mensajes={}), enviando como audio", conversacion.getTotalMensajes());
            return true;
        }

        // Si el cliente pidió recomendación, explicación, etc.
        if (mensajeCliente != null && KEYWORDS_VOZ.matcher(mensajeCliente).find()) {
            log.info("VOZ: keyword detectado en mensaje del cliente: '{}'",
                    mensajeCliente.substring(0, Math.min(mensajeCliente.length(), 50)));
            return true;
        }

        // Si la respuesta tiene mas de 20 palabras → voz (es una explicacion)
        int palabras = texto.split("\\s+").length;
        if (palabras > 20) {
            log.info("VOZ: respuesta larga ({} palabras), enviando como audio", palabras);
            return true;
        }

        // Random ~20% de las veces
        boolean random = ThreadLocalRandom.current().nextInt(100) < 20;
        log.info("VOZ: random={}, palabras={}, mensajes={}", random, palabras, conversacion.getTotalMensajes());
        return random;
    }

    /**
     * Envía una nota de voz por WhatsApp via ElevenLabs + Baileys
     */
    private boolean enviarAudio(ConfiguracionBot config, String jid, String texto) {
        try {
            // 1. Convertir texto a audio con ElevenLabs
            byte[] audioBytes = elevenLabsService.convertirTextoAAudio(texto);
            if (audioBytes == null || audioBytes.length == 0) {
                log.warn("ElevenLabs no generó audio");
                return false;
            }

            // 2. Enviar audio como nota de voz via Baileys
            String url = buildBaileysUrl(config, "/send-audio");
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

            java.util.Map<String, String> body = java.util.Map.of(
                    "jid", jid,
                    "audioBase64", audioBase64
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<java.util.Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, java.util.Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Nota de voz enviada exitosamente a {} ({} bytes)", jid, audioBytes.length);
                return true;
            } else {
                log.warn("Error enviando nota de voz: status={}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error enviando nota de voz a WhatsApp: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envía una imagen por WhatsApp via Baileys
     */
    private void enviarImagen(ConfiguracionBot config, String jid, String imageUrl, String caption) {
        try {
            String url = buildBaileysUrl(config, "/send-image");

            java.util.Map<String, String> body = java.util.Map.of(
                    "jid", jid,
                    "imageUrl", imageUrl,
                    "caption", caption != null ? caption : ""
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<java.util.Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, java.util.Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Imagen enviada exitosamente a {}", jid);
            } else {
                log.warn("Error enviando imagen: status={}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error enviando imagen a WhatsApp: {}", e.getMessage());
        }
    }
}
