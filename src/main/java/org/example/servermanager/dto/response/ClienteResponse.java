package org.example.servermanager.dto.response;

import org.example.servermanager.entity.Cliente;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        Long empresaId,
        String telefono,
        String nombre,
        String email,
        String distrito,
        String direccion,
        Integer totalConversaciones,
        Integer totalPedidos,
        LocalDateTime ultimaInteraccion,
        LocalDateTime fechaCreacion
) {
    public static ClienteResponse fromEntity(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getEmpresa().getId(),
                cliente.getTelefono(),
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getDistrito(),
                cliente.getDireccion(),
                cliente.getTotalConversaciones(),
                cliente.getTotalPedidos(),
                cliente.getUltimaInteraccion(),
                cliente.getFechaCreacion()
        );
    }
}
