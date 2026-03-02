package org.example.servermanager.dto.response;

import org.example.servermanager.entity.Campana;
import org.example.servermanager.enums.EstadoCampana;

import java.time.LocalDateTime;

public record CampanaResponse(
        Long id,
        Long empresaId,
        String nombre,
        String groupJid,
        String groupNombre,
        String promptMensaje,
        String infoNegocio,
        String linkPrecios,
        String beneficiosGrupo,
        String linkGrupoInvitacion,
        Integer delaySegundos,
        EstadoCampana estado,
        Integer totalContactos,
        Integer totalEnviados,
        Integer totalFallidos,
        Double progreso,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {
    public static CampanaResponse fromEntity(Campana c) {
        double progreso = c.getTotalContactos() > 0
                ? (double) (c.getTotalEnviados() + c.getTotalFallidos()) / c.getTotalContactos() * 100
                : 0.0;

        return new CampanaResponse(
                c.getId(),
                c.getEmpresa().getId(),
                c.getNombre(),
                c.getGroupJid(),
                c.getGroupNombre(),
                c.getPromptMensaje(),
                c.getInfoNegocio(),
                c.getLinkPrecios(),
                c.getBeneficiosGrupo(),
                c.getLinkGrupoInvitacion(),
                c.getDelaySegundos(),
                c.getEstado(),
                c.getTotalContactos(),
                c.getTotalEnviados(),
                c.getTotalFallidos(),
                Math.round(progreso * 100.0) / 100.0,
                c.getFechaCreacion(),
                c.getFechaInicio(),
                c.getFechaFin()
        );
    }
}
