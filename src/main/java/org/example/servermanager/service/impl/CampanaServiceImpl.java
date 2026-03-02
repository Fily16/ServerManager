package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.evolution.GroupParticipant;
import org.example.servermanager.dto.openai.ChatCompletionRequest;
import org.example.servermanager.dto.openai.ChatCompletionResponse;
import org.example.servermanager.dto.openai.ChatMessage;
import org.example.servermanager.dto.request.CampanaRequest;
import org.example.servermanager.dto.response.CampanaResponse;
import org.example.servermanager.dto.response.MensajeCampanaResponse;
import org.example.servermanager.entity.Campana;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.entity.MensajeCampana;
import org.example.servermanager.enums.EstadoCampana;
import org.example.servermanager.enums.EstadoMensajeCampana;
import org.example.servermanager.exception.BusinessException;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.CampanaRepository;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.repository.MensajeCampanaRepository;
import org.example.servermanager.entity.ConfiguracionBot;
import org.example.servermanager.repository.ConfiguracionBotRepository;
import org.example.servermanager.service.CampanaService;
import org.example.servermanager.service.ConfiguracionSaasService;
import org.example.servermanager.service.EvolutionGroupService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampanaServiceImpl implements CampanaService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final CampanaRepository campanaRepository;
    private final MensajeCampanaRepository mensajeCampanaRepository;
    private final EmpresaRepository empresaRepository;
    private final ConfiguracionBotRepository configuracionBotRepository;
    private final EvolutionGroupService evolutionGroupService;
    private final ConfiguracionSaasService configuracionSaasService;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public CampanaResponse crear(Long empresaId, CampanaRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        // 1. Obtener config del bot para auto-rellenar datos
        ConfiguracionBot botConfig = configuracionBotRepository.findByEmpresaId(empresaId).orElse(null);

        // Auto-rellenar prompt si no viene del frontend
        String promptMensaje = request.promptMensaje();
        if (promptMensaje == null || promptMensaje.isBlank()) {
            promptMensaje = "Hola! vi que estamos en el mismo grupo, te escribo xq tenemos perfumes arabes a super precios, te paso el catalogo y si te interesa te meto al grupo";
        }

        // Auto-rellenar link del grupo desde config
        String linkGrupo = request.linkGrupoInvitacion();
        if ((linkGrupo == null || linkGrupo.isBlank()) && botConfig != null && botConfig.getLinkGrupoConsolidado() != null) {
            linkGrupo = botConfig.getLinkGrupoConsolidado();
        }

        // Auto-rellenar link de precios desde config
        String linkPrecios = request.linkPrecios();
        if ((linkPrecios == null || linkPrecios.isBlank()) && botConfig != null && botConfig.getLinkCatalogo() != null) {
            linkPrecios = botConfig.getLinkCatalogo();
        }

        // Auto-rellenar nombre si no viene
        String nombre = request.nombre();
        if (nombre == null || nombre.isBlank()) {
            nombre = request.groupNombre() != null ? request.groupNombre() : "Campaña " + LocalDateTime.now().toLocalDate();
        }

        // Crear campana
        Campana campana = Campana.builder()
                .empresa(empresa)
                .nombre(nombre)
                .groupJid(request.groupJid())
                .groupNombre(request.groupNombre())
                .promptMensaje(promptMensaje)
                .infoNegocio(request.infoNegocio())
                .linkPrecios(linkPrecios)
                .beneficiosGrupo(request.beneficiosGrupo())
                .linkGrupoInvitacion(linkGrupo)
                .delaySegundos(request.delaySegundos())
                .estado(EstadoCampana.CARGANDO_CONTACTOS)
                .build();
        campana = campanaRepository.save(campana);

        // 2. Cargar contactos del grupo
        List<GroupParticipant> participantes = evolutionGroupService.obtenerParticipantes(
                empresaId, request.groupJid());

        // Filtrar administradores del grupo (no enviarles mensajes de campaña)
        participantes = participantes.stream()
                .filter(p -> !p.isAdmin())
                .toList();

        if (participantes.isEmpty()) {
            campana.setEstado(EstadoCampana.ERROR);
            campanaRepository.save(campana);
            throw new BusinessException("EMPTY_GROUP", "El grupo no tiene participantes o no se pudo acceder");
        }

        // 2b. Filtrar contactos que ya recibieron mensaje en campanas anteriores del mismo grupo
        Set<String> yaEnviados = new HashSet<>(
                mensajeCampanaRepository.findTelefonosYaEnviadosPorGrupo(request.groupJid()));

        if (!yaEnviados.isEmpty()) {
            int totalAntes = participantes.size();
            participantes = participantes.stream()
                    .filter(p -> !yaEnviados.contains(p.getTelefono()))
                    .toList();
            int omitidos = totalAntes - participantes.size();
            if (omitidos > 0) {
                log.info("Campana para grupo {}: omitidos {} contactos ya contactados previamente",
                        request.groupJid(), omitidos);
            }
        }

        if (participantes.isEmpty()) {
            campana.setEstado(EstadoCampana.COMPLETADA);
            campana.setTotalContactos(0);
            campanaRepository.save(campana);
            throw new BusinessException("ALL_CONTACTED", "Todos los miembros de este grupo ya fueron contactados en campanas anteriores");
        }

        // 3. Crear mensajes pendientes para cada participante (SIN contenido, se genera al enviar)
        AtomicInteger orden = new AtomicInteger(1);
        List<MensajeCampana> mensajes = new ArrayList<>();

        for (GroupParticipant p : participantes) {
            MensajeCampana msg = MensajeCampana.builder()
                    .campana(campana)
                    .telefono(p.getTelefono())
                    .nombre(null)
                    .estado(EstadoMensajeCampana.PENDIENTE)
                    .orden(orden.getAndIncrement())
                    .build();
            mensajes.add(msg);
        }

        mensajeCampanaRepository.saveAll(mensajes);

        // 4. Marcar como CREADA directamente (los mensajes se generan uno por uno al enviar)
        campana.setTotalContactos(mensajes.size());
        campana.setEstado(EstadoCampana.CREADA);
        campana = campanaRepository.save(campana);

        log.info("Campana {} creada con {} contactos. Los mensajes se generaran al enviar.",
                campana.getId(), mensajes.size());

        return CampanaResponse.fromEntity(campana);
    }

    @Override
    public String generarMensajeIndividual(Long campanaId) {
        Campana campana = campanaRepository.findById(campanaId)
                .orElseThrow(() -> new ResourceNotFoundException("Campana", "id", campanaId));

        Long empresaId = campana.getEmpresa().getId();
        ConfiguracionBot botConfig = configuracionBotRepository.findByEmpresaId(empresaId).orElse(null);

        // Construir el contenido base del mensaje (se usa tanto para OpenAI como para fallback)
        String contenidoBase = construirContenidoMensaje(campana, botConfig);

        // Intentar reescribir con OpenAI
        String apiKey = null;
        try {
            apiKey = configuracionSaasService.obtenerOpenAiApiKey();
        } catch (Exception e) {
            log.warn("No se pudo obtener API key de OpenAI: {}", e.getMessage());
        }

        if (apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("sk-tu-")) {
            // System prompt: SOLO reglas de tono, nada de contenido
            String systemPrompt = """
                    Eres un generador de mensajes de WhatsApp. Tu trabajo es tomar el texto que te de el usuario
                    y reescribirlo como un mensaje de WhatsApp natural, variando ligeramente el estilo cada vez.

                    REGLAS DE FORMATO:
                    - Maximo 5-6 lineas
                    - Emojis con moderacion (2-3 maximo)
                    - Tono profesional y empresarial, pero cercano y amigable
                    - NO uses jerga: nada de "bacan", "chevere", "pe", "causa", "xq", "q tal"
                    - NO digas "vi que te gustan los perfumes", "estamos en el mismo grupo", "tenemos gustos en comun"
                    - NO uses "Estimado/a", "Cordial saludo", ni "Buenos dias/tardes"
                    - Varia el estilo en cada generacion (a veces pregunta, a veces directo, etc)
                    - DEBES incluir TODOS los links que aparezcan en el texto
                    - SOLO devuelve el mensaje final, nada mas
                    """;

            String userPrompt = "Reescribe esto como UN mensaje de WhatsApp natural. Incluye TODOS los links:\n\n" + contenidoBase;

            List<ChatMessage> chatMessages = List.of(
                    ChatMessage.system(systemPrompt),
                    ChatMessage.user(userPrompt)
            );

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(chatMessages)
                    .maxTokens(300)
                    .temperature(0.85)
                    .build();

            log.info("Campana {}: llamando OpenAI con API key {}...", campanaId,
                    apiKey.substring(0, Math.min(10, apiKey.length())) + "***");

            ChatCompletionResponse response = callOpenAI(apiKey, request);

            if (response != null && response.getResponseText() != null) {
                String msg = response.getResponseText().trim();
                msg = msg.replaceFirst("^\\d+[.)\\-]\\s*", "");
                msg = msg.replaceAll("^\"|\"$", "");
                log.info("Campana {}: mensaje generado por OpenAI exitosamente", campanaId);
                return msg;
            }

            log.warn("Campana {}: OpenAI retorno null, usando contenido configurado como fallback", campanaId);
        } else {
            log.warn("Campana {}: API key no configurada (valor: {}), usando contenido configurado directamente",
                    campanaId, apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "***" : "null");
        }

        // Fallback: usar el contenido configurado directamente (NO un mensaje generico hardcodeado)
        return contenidoBase;
    }

    /**
     * Construye el contenido del mensaje usando el prompt configurado en el dashboard + links.
     * Se usa tanto como input para OpenAI como fallback directo si OpenAI falla.
     */
    private String construirContenidoMensaje(Campana campana, ConfiguracionBot botConfig) {
        StringBuilder contenido = new StringBuilder();

        // Usar prompt personalizado del dashboard si existe
        if (botConfig != null && botConfig.getPromptCampana() != null && !botConfig.getPromptCampana().isBlank()) {
            contenido.append(botConfig.getPromptCampana());
        } else {
            contenido.append("Hola, somos Aroma Studio. Nos dedicamos a importar perfumes arabes y hacemos consolidados al mejor precio. ");
            contenido.append("Tenemos un grupo VIP donde publicamos perfumes que ya tenemos en stock en Peru. ");
            contenido.append("Por ser parte del Grupo VIP, tendras acceso a precios especiales, mejores que los del publico general. ");
            contenido.append("Te gustaria ver nuestro catalogo?");
        }

        // Agregar links
        contenido.append("\n\n");
        if (campana.getLinkGrupoInvitacion() != null && !campana.getLinkGrupoInvitacion().isBlank()) {
            contenido.append("Grupo VIP: ").append(campana.getLinkGrupoInvitacion()).append("\n");
        }
        if (campana.getLinkPrecios() != null && !campana.getLinkPrecios().isBlank()) {
            contenido.append("Catalogo: ").append(campana.getLinkPrecios()).append("\n");
        }
        if (botConfig != null && botConfig.getLinkTiktok() != null && !botConfig.getLinkTiktok().isBlank()) {
            contenido.append("TikTok: ").append(botConfig.getLinkTiktok()).append("\n");
        }

        return contenido.toString().trim();
    }

    @Override
    public List<CampanaResponse> listarPorEmpresa(Long empresaId) {
        return campanaRepository.findByEmpresaIdOrderByFechaCreacionDesc(empresaId).stream()
                .map(CampanaResponse::fromEntity)
                .toList();
    }

    @Override
    public CampanaResponse obtenerPorId(Long empresaId, Long campanaId) {
        Campana campana = getCampana(empresaId, campanaId);
        return CampanaResponse.fromEntity(campana);
    }

    @Override
    public List<MensajeCampanaResponse> listarMensajes(Long campanaId) {
        return mensajeCampanaRepository.findByCampanaIdOrderByOrdenAsc(campanaId).stream()
                .map(MensajeCampanaResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public CampanaResponse iniciar(Long empresaId, Long campanaId) {
        Campana campana = getCampana(empresaId, campanaId);

        if (campana.getEstado() != EstadoCampana.CREADA) {
            throw new BusinessException("INVALID_STATE",
                    "Solo se puede iniciar una campana en estado CREADA. Estado actual: " + campana.getEstado());
        }

        campana.setEstado(EstadoCampana.EN_PROGRESO);
        campana.setFechaInicio(LocalDateTime.now());
        campana = campanaRepository.save(campana);

        log.info("Campana {} iniciada con {} contactos, delay {}s",
                campanaId, campana.getTotalContactos(), campana.getDelaySegundos());

        return CampanaResponse.fromEntity(campana);
    }

    @Override
    @Transactional
    public CampanaResponse pausar(Long empresaId, Long campanaId) {
        Campana campana = getCampana(empresaId, campanaId);

        if (campana.getEstado() != EstadoCampana.EN_PROGRESO) {
            throw new BusinessException("INVALID_STATE", "Solo se puede pausar una campana EN_PROGRESO");
        }

        campana.setEstado(EstadoCampana.PAUSADA);
        campana = campanaRepository.save(campana);
        log.info("Campana {} pausada. Enviados: {}/{}", campanaId, campana.getTotalEnviados(), campana.getTotalContactos());

        return CampanaResponse.fromEntity(campana);
    }

    @Override
    @Transactional
    public CampanaResponse reanudar(Long empresaId, Long campanaId) {
        Campana campana = getCampana(empresaId, campanaId);

        if (campana.getEstado() != EstadoCampana.PAUSADA) {
            throw new BusinessException("INVALID_STATE", "Solo se puede reanudar una campana PAUSADA");
        }

        campana.setEstado(EstadoCampana.EN_PROGRESO);
        campana = campanaRepository.save(campana);
        log.info("Campana {} reanudada", campanaId);

        return CampanaResponse.fromEntity(campana);
    }

    @Override
    @Transactional
    public CampanaResponse cancelar(Long empresaId, Long campanaId) {
        Campana campana = getCampana(empresaId, campanaId);

        if (campana.getEstado() == EstadoCampana.COMPLETADA || campana.getEstado() == EstadoCampana.CANCELADA) {
            throw new BusinessException("INVALID_STATE", "La campana ya esta " + campana.getEstado());
        }

        // Marcar pendientes como omitidos
        mensajeCampanaRepository.findByCampanaIdAndEstado(campanaId, EstadoMensajeCampana.PENDIENTE)
                .forEach(m -> {
                    m.setEstado(EstadoMensajeCampana.OMITIDO);
                    mensajeCampanaRepository.save(m);
                });

        campana.setEstado(EstadoCampana.CANCELADA);
        campana.setFechaFin(LocalDateTime.now());
        campana = campanaRepository.save(campana);
        log.info("Campana {} cancelada. Enviados: {}/{}", campanaId, campana.getTotalEnviados(), campana.getTotalContactos());

        return CampanaResponse.fromEntity(campana);
    }

    @Override
    @Transactional
    public CampanaResponse cambiarDelay(Long empresaId, Long campanaId, Integer nuevoDelaySegundos) {
        Campana campana = getCampana(empresaId, campanaId);

        if (nuevoDelaySegundos < 10 || nuevoDelaySegundos > 600) {
            throw new BusinessException("INVALID_DELAY", "El delay debe estar entre 10 y 600 segundos");
        }

        campana.setDelaySegundos(nuevoDelaySegundos);
        campana = campanaRepository.save(campana);
        log.info("Campana {} delay cambiado a {}s", campanaId, nuevoDelaySegundos);

        return CampanaResponse.fromEntity(campana);
    }

    // ==================== METODOS PRIVADOS ====================

    private Campana getCampana(Long empresaId, Long campanaId) {
        return campanaRepository.findById(campanaId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new ResourceNotFoundException("Campana", "id", campanaId));
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
                log.debug("OpenAI respuesta exitosa: {} tokens usados",
                        response.getBody().getUsage() != null ? response.getBody().getUsage().getTotalTokens() : "?");
                return response.getBody();
            }
            log.warn("OpenAI respuesta no exitosa: status={}", response.getStatusCode());
            return null;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("OpenAI error HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Error llamando a OpenAI: [{}] {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }
}
