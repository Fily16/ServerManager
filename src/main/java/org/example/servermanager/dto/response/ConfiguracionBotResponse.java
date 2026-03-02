package org.example.servermanager.dto.response;

import org.example.servermanager.entity.ConfiguracionBot;
import org.example.servermanager.enums.TonoConversacion;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ConfiguracionBotResponse(
        Long id,
        Long empresaId,
        String evolutionApiUrl,
        String evolutionInstancia,
        String numeroWhatsapp,
        String nombreBot,
        String mensajeBienvenida,
        String promptSistema,
        TonoConversacion tonoConversacion,
        String modeloAi,
        Boolean verificacionPagosActivo,
        String emailNotificacionesPago,
        String linkGrupoConsolidado,
        String linkCatalogo,
        String tiempoEntrega,
        String linkTiktok,
        String promptCampana,
        LocalTime horarioInicio,
        LocalTime horarioFin,
        String mensajeFueraHorario,
        Boolean autoRespuesta,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    public static ConfiguracionBotResponse fromEntity(ConfiguracionBot config) {
        return new ConfiguracionBotResponse(
                config.getId(),
                config.getEmpresa().getId(),
                config.getEvolutionApiUrl(),
                config.getEvolutionInstancia(),
                config.getNumeroWhatsapp(),
                config.getNombreBot(),
                config.getMensajeBienvenida(),
                config.getPromptSistema(),
                config.getTonoConversacion(),
                config.getModeloAi(),
                config.getVerificacionPagosActivo(),
                config.getEmailNotificacionesPago(),
                config.getLinkGrupoConsolidado(),
                config.getLinkCatalogo(),
                config.getTiempoEntrega(),
                config.getLinkTiktok(),
                config.getPromptCampana(),
                config.getHorarioInicio(),
                config.getHorarioFin(),
                config.getMensajeFueraHorario(),
                config.getAutoRespuesta(),
                config.getActivo(),
                config.getFechaCreacion(),
                config.getFechaActualizacion()
        );
    }
}
