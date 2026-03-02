package org.example.servermanager.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.webhook.SendMessageResponse;
import org.example.servermanager.entity.Campana;
import org.example.servermanager.entity.MensajeCampana;
import org.example.servermanager.enums.EstadoCampana;
import org.example.servermanager.enums.EstadoMensajeCampana;
import org.example.servermanager.repository.CampanaRepository;
import org.example.servermanager.repository.MensajeCampanaRepository;
import org.example.servermanager.service.CampanaService;
import org.example.servermanager.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CampanaScheduler {

    private final CampanaRepository campanaRepository;
    private final MensajeCampanaRepository mensajeCampanaRepository;
    private final WhatsAppService whatsAppService;

    @Lazy
    @Autowired
    private CampanaService campanaService;

    /** Guarda el ultimo envio de cada campana para respetar el delay */
    private final Map<Long, LocalDateTime> ultimoEnvioPorCampana = new ConcurrentHashMap<>();

    public CampanaScheduler(CampanaRepository campanaRepository,
                            MensajeCampanaRepository mensajeCampanaRepository,
                            WhatsAppService whatsAppService) {
        this.campanaRepository = campanaRepository;
        this.mensajeCampanaRepository = mensajeCampanaRepository;
        this.whatsAppService = whatsAppService;
    }

    /**
     * Se ejecuta cada 10 segundos.
     * Busca campanas EN_PROGRESO y envia el siguiente mensaje
     * si ya paso el delay configurado desde el ultimo envio.
     * Flujo: generar mensaje con IA → enviar → esperar delay → siguiente
     */
    @Scheduled(fixedDelay = 10_000)
    @Transactional
    public void procesarCampanasActivas() {
        List<Campana> activas = campanaRepository.findByEstado(EstadoCampana.EN_PROGRESO);

        for (Campana campana : activas) {
            try {
                procesarCampana(campana);
            } catch (Exception e) {
                log.error("Error procesando campana {}: {}", campana.getId(), e.getMessage());
            }
        }
    }

    private void procesarCampana(Campana campana) {
        // Verificar si ya paso el delay desde el ultimo envio
        LocalDateTime ultimoEnvio = ultimoEnvioPorCampana.get(campana.getId());
        if (ultimoEnvio != null) {
            long segundosDesdeUltimo = ChronoUnit.SECONDS.between(ultimoEnvio, LocalDateTime.now());
            if (segundosDesdeUltimo < campana.getDelaySegundos()) {
                return; // Aun no es momento de enviar
            }
        }

        // Obtener siguiente mensaje pendiente
        Optional<MensajeCampana> siguienteOpt = mensajeCampanaRepository
                .findSiguientePendiente(campana.getId());

        if (siguienteOpt.isEmpty()) {
            completarCampana(campana);
            return;
        }

        MensajeCampana mensaje = siguienteOpt.get();

        // 1. Generar mensaje personalizado con IA (solo si no tiene contenido)
        if (mensaje.getContenido() == null || mensaje.getContenido().isBlank()) {
            log.info("Campana {}: generando mensaje para {} ({}/{})",
                    campana.getId(), mensaje.getTelefono(), mensaje.getOrden(), campana.getTotalContactos());
            try {
                String contenido = campanaService.generarMensajeIndividual(campana.getId());
                mensaje.setContenido(contenido);
                mensajeCampanaRepository.save(mensaje);
            } catch (Exception e) {
                log.error("Campana {}: error generando mensaje: {}", campana.getId(), e.getMessage());
                // No usar mensaje hardcodeado - generarMensajeIndividual ya retorna el fallback configurado
                // Si llega aqui es un error critico, usar texto minimo
                mensaje.setContenido("Hola, somos Aroma Studio. Te gustaria conocer nuestro catalogo de perfumes arabes?");
                mensajeCampanaRepository.save(mensaje);
            }
        }

        // 2. Enviar mensaje
        enviarMensaje(campana, mensaje);
    }

    private void enviarMensaje(Campana campana, MensajeCampana mensaje) {
        Long empresaId = campana.getEmpresa().getId();
        String telefono = mensaje.getTelefono();
        String contenido = mensaje.getContenido();

        log.info("Campana {}: enviando a {} ({}/{})",
                campana.getId(), telefono, mensaje.getOrden(), campana.getTotalContactos());

        try {
            SendMessageResponse response = whatsAppService.enviarMensaje(empresaId, telefono, contenido);

            if (response != null && response.isSuccess()) {
                mensaje.setEstado(EstadoMensajeCampana.ENVIADO);
                mensaje.setWhatsappMessageId(response.getMessageId());
                mensaje.setFechaEnvio(LocalDateTime.now());
                campana.setTotalEnviados(campana.getTotalEnviados() + 1);

                log.info("Campana {}: enviado a {} ({}/{})",
                        campana.getId(), telefono, campana.getTotalEnviados(), campana.getTotalContactos());
            } else {
                mensaje.setEstado(EstadoMensajeCampana.FALLIDO);
                mensaje.setErrorDetalle("WhatsApp no retorno exito");
                mensaje.setFechaEnvio(LocalDateTime.now());
                campana.setTotalFallidos(campana.getTotalFallidos() + 1);
                log.warn("Campana {}: fallo envio a {}", campana.getId(), telefono);
            }
        } catch (Exception e) {
            mensaje.setEstado(EstadoMensajeCampana.FALLIDO);
            mensaje.setErrorDetalle(e.getMessage() != null
                    ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 255))
                    : "Error desconocido");
            mensaje.setFechaEnvio(LocalDateTime.now());
            campana.setTotalFallidos(campana.getTotalFallidos() + 1);
            log.error("Campana {}: error enviando a {}: {}", campana.getId(), telefono, e.getMessage());
        }

        mensajeCampanaRepository.save(mensaje);
        campanaRepository.save(campana);

        // Registrar momento del envio para controlar el delay
        ultimoEnvioPorCampana.put(campana.getId(), LocalDateTime.now());
    }

    private void completarCampana(Campana campana) {
        campana.setEstado(EstadoCampana.COMPLETADA);
        campana.setFechaFin(LocalDateTime.now());
        campanaRepository.save(campana);

        ultimoEnvioPorCampana.remove(campana.getId());

        log.info("Campana {} COMPLETADA. Enviados: {}, Fallidos: {}, Total: {}",
                campana.getId(),
                campana.getTotalEnviados(),
                campana.getTotalFallidos(),
                campana.getTotalContactos());
    }
}
