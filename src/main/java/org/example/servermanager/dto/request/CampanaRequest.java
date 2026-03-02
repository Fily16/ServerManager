package org.example.servermanager.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CampanaRequest(

        String nombre,

        @NotBlank(message = "El groupJid es requerido")
        String groupJid,

        String groupNombre,

        String promptMensaje,

        /** Info del negocio: productos, precios, que vendes, etc */
        String infoNegocio,

        /** Link de Excel/Drive con catalogo de precios */
        String linkPrecios,

        /** Beneficios de unirse a tu grupo */
        String beneficiosGrupo,

        /** Link de invitacion a tu grupo de WhatsApp */
        String linkGrupoInvitacion,

        @Min(value = 10, message = "El delay minimo es 10 segundos")
        @Max(value = 600, message = "El delay maximo es 600 segundos (10 min)")
        Integer delaySegundos
) {
    public CampanaRequest {
        if (delaySegundos == null) delaySegundos = 120;
    }
}
