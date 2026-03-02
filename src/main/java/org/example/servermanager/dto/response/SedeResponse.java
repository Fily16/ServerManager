package org.example.servermanager.dto.response;

import org.example.servermanager.entity.Sede;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SedeResponse(
        Long id,
        Long empresaId,
        String nombre,
        String direccion,
        String distrito,
        String ciudad,
        String referencia,
        String telefono,
        String horarioAtencion,
        BigDecimal latitud,
        BigDecimal longitud,
        Boolean esPrincipal,
        Boolean activo,
        LocalDateTime fechaCreacion
) {
    public static SedeResponse fromEntity(Sede sede) {
        return new SedeResponse(
                sede.getId(),
                sede.getEmpresa().getId(),
                sede.getNombre(),
                sede.getDireccion(),
                sede.getDistrito(),
                sede.getCiudad(),
                sede.getReferencia(),
                sede.getTelefono(),
                sede.getHorarioAtencion(),
                sede.getLatitud(),
                sede.getLongitud(),
                sede.getEsPrincipal(),
                sede.getActivo(),
                sede.getFechaCreacion()
        );
    }
}
