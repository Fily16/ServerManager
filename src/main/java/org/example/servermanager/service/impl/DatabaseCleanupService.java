package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.repository.MensajeCampanaRepository;
import org.example.servermanager.repository.MensajeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Limpieza automática de la base de datos para mantener el uso de espacio bajo control.
 * Se ejecuta cada 24 horas y elimina mensajes antiguos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupService {

    private final MensajeRepository mensajeRepository;
    private final MensajeCampanaRepository mensajeCampanaRepository;

    private static final int DIAS_RETENCION_MENSAJES = 90;
    private static final int DIAS_RETENCION_CAMPANA = 30;

    /**
     * Cada 24 horas elimina mensajes de conversación con más de 90 días
     * y mensajes de campaña con más de 30 días.
     */
    @Scheduled(fixedRate = 86400000) // 24 horas en ms
    @Transactional
    public void limpiarMensajesAntiguos() {
        try {
            LocalDateTime limiteMensajes = LocalDateTime.now().minusDays(DIAS_RETENCION_MENSAJES);
            int mensajesEliminados = mensajeRepository.deleteByFechaEnvioBefore(limiteMensajes);

            LocalDateTime limiteCampana = LocalDateTime.now().minusDays(DIAS_RETENCION_CAMPANA);
            int campanaEliminados = mensajeCampanaRepository.deleteByFechaCreacionBefore(limiteCampana);

            if (mensajesEliminados > 0 || campanaEliminados > 0) {
                log.info("Limpieza BD: {} mensajes (>{}d) y {} mensajes de campaña (>{}d) eliminados",
                        mensajesEliminados, DIAS_RETENCION_MENSAJES,
                        campanaEliminados, DIAS_RETENCION_CAMPANA);
            }
        } catch (Exception e) {
            log.error("Error en limpieza automática de BD: {}", e.getMessage());
        }
    }
}
