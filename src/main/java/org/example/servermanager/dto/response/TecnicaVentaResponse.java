package org.example.servermanager.dto.response;

import org.example.servermanager.entity.TecnicaVenta;

import java.time.LocalDateTime;

public record TecnicaVentaResponse(
        Long id,
        String categoria,
        String nombre,
        String descripcion,
        String ejemplo,
        String fuente,
        Integer prioridad,
        Boolean activo,
        LocalDateTime fechaCreacion
) {
    public static TecnicaVentaResponse fromEntity(TecnicaVenta entity) {
        return new TecnicaVentaResponse(
                entity.getId(),
                entity.getCategoria(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getEjemplo(),
                entity.getFuente(),
                entity.getPrioridad(),
                entity.getActivo(),
                entity.getFechaCreacion()
        );
    }
}
