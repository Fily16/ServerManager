package org.example.servermanager.dto.response;

import org.example.servermanager.entity.Conversacion;
import org.example.servermanager.enums.EstadoConversacion;

import java.time.LocalDateTime;
import java.util.List;

public record ConversacionResponse(
        Long id,
        Long empresaId,
        Long clienteId,
        String telefonoCliente,
        String nombreCliente,
        EstadoConversacion estado,
        String contextoAi,
        Integer totalMensajes,
        LocalDateTime fechaInicio,
        LocalDateTime fechaUltimoMensaje,
        LocalDateTime fechaCierre,
        List<MensajeResponse> mensajes
) {
    public static ConversacionResponse fromEntity(Conversacion conversacion) {
        return fromEntity(conversacion, false);
    }

    public static ConversacionResponse fromEntity(Conversacion conversacion, boolean includeMensajes) {
        List<MensajeResponse> mensajesResponse = null;
        if (includeMensajes && conversacion.getMensajes() != null) {
            mensajesResponse = conversacion.getMensajes().stream()
                    .map(MensajeResponse::fromEntity)
                    .toList();
        }

        return new ConversacionResponse(
                conversacion.getId(),
                conversacion.getEmpresa().getId(),
                conversacion.getCliente() != null ? conversacion.getCliente().getId() : null,
                conversacion.getTelefonoCliente(),
                conversacion.getNombreCliente(),
                conversacion.getEstado(),
                conversacion.getContextoAi(),
                conversacion.getTotalMensajes(),
                conversacion.getFechaInicio(),
                conversacion.getFechaUltimoMensaje(),
                conversacion.getFechaCierre(),
                mensajesResponse
        );
    }
}
