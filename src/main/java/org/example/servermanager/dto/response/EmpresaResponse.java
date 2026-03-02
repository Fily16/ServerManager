package org.example.servermanager.dto.response;

import org.example.servermanager.entity.Empresa;
import org.example.servermanager.enums.Plan;

import java.time.LocalDateTime;

public record EmpresaResponse(
        Long id,
        String nombre,
        String ruc,
        String email,
        String telefono,
        String logoUrl,
        String direccionFiscal,
        Plan plan,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    public static EmpresaResponse fromEntity(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getRuc(),
                empresa.getEmail(),
                empresa.getTelefono(),
                empresa.getLogoUrl(),
                empresa.getDireccionFiscal(),
                empresa.getPlan(),
                empresa.getActivo(),
                empresa.getFechaCreacion(),
                empresa.getFechaActualizacion()
        );
    }
}
