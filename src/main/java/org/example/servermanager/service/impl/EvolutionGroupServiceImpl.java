package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.evolution.GroupInfo;
import org.example.servermanager.dto.evolution.GroupParticipant;
import org.example.servermanager.entity.ConfiguracionBot;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.ConfiguracionBotRepository;
import org.example.servermanager.service.EvolutionGroupService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvolutionGroupServiceImpl implements EvolutionGroupService {

    private final ConfiguracionBotRepository configuracionBotRepository;
    private final RestTemplate restTemplate;

    @Override
    public List<GroupInfo> listarGrupos(Long empresaId) {
        ConfiguracionBot config = getConfig(empresaId);

        // Usamos nuestro método blindado para construir la URL
        String url = buildBaileysUrl(config, "/groups");

        log.info("Listando grupos para empresa {} desde WhatsApp Service", empresaId);
        log.info("URL de peticion: {}", url);

        try {
            ResponseEntity<List<GroupInfo>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            List<GroupInfo> grupos = response.getBody();
            if (grupos == null) return Collections.emptyList();

            log.info("Se encontraron {} grupos para empresa {}", grupos.size(), empresaId);
            return grupos;

        } catch (Exception e) {
            log.error("Error listando grupos para empresa {}: [{}] {}",
                    empresaId, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener grupos de WhatsApp: ["
                    + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    @Override
    public List<GroupParticipant> obtenerParticipantes(Long empresaId, String groupJid) {
        ConfiguracionBot config = getConfig(empresaId);

        // Usamos nuestro método blindado para construir la URL
        String url = buildBaileysUrl(config, "/groups/" + groupJid + "/participants");

        log.info("Obteniendo participantes del grupo {} para empresa {}", groupJid, empresaId);

        try {
            ResponseEntity<List<GroupParticipant>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            List<GroupParticipant> participantes = response.getBody();
            if (participantes == null) return Collections.emptyList();

            // Filtrar: excluir el propio numero del bot
            String numeroPropioJid = config.getNumeroWhatsapp() != null
                    ? config.getNumeroWhatsapp() + "@s.whatsapp.net"
                    : null;

            List<GroupParticipant> filtrados = participantes.stream()
                    .filter(p -> numeroPropioJid == null || !p.getId().equals(numeroPropioJid))
                    .toList();

            log.info("Se encontraron {} participantes en grupo {} (excluido bot)",
                    filtrados.size(), groupJid);
            return filtrados;

        } catch (Exception e) {
            log.error("Error obteniendo participantes del grupo {}: {}", groupJid, e.getMessage());
            throw new RuntimeException("Error al obtener participantes: " + e.getMessage());
        }
    }

    private ConfiguracionBot getConfig(Long empresaId) {
        return configuracionBotRepository.findByEmpresaId(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("ConfiguracionBot", "empresaId", empresaId));
    }

    // --- EL BLINDAJE PARA LA URL ---
    private String buildBaileysUrl(ConfiguracionBot config, String endpoint) {
        String baseUrl = config.getEvolutionApiUrl();

        // Si la base de datos devuelve basura o no tiene http, forzamos la de Render
        if (baseUrl == null || !baseUrl.startsWith("http")) {
            log.warn("ATENCION (Grupos): La URL en la BD era invalida ('{}'). Forzando la URL real de Render.", baseUrl);
            baseUrl = "https://whatsapp-baileys-bot.onrender.com/api";
        }

        baseUrl = baseUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + "/" + config.getEvolutionInstancia() + endpoint;
    }
}