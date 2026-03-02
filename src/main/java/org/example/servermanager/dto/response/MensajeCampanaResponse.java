package org.example.servermanager.dto.response;

import org.example.servermanager.entity.MensajeCampana;
import org.example.servermanager.enums.EstadoMensajeCampana;

import java.time.LocalDateTime;

public record MensajeCampanaResponse(
        Long id,
        String telefono,
        String nombre,
        String contenido,
        EstadoMensajeCampana estado,
        String errorDetalle,
        Integer orden,
        LocalDateTime fechaEnvio
) {
    public static MensajeCampanaResponse fromEntity(MensajeCampana m) {
        return new MensajeCampanaResponse(
                m.getId(),
                m.getTelefono(),
                m.getNombre(),
                m.getContenido(),
                m.getEstado(),
                m.getErrorDetalle(),
                m.getOrden(),
                m.getFechaEnvio()
        );
    }
}
