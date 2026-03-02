package org.example.servermanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SedeRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String nombre,

        @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
        String direccion,

        @Size(max = 100, message = "El distrito no puede exceder 100 caracteres")
        String distrito,

        @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
        String ciudad,

        String referencia,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono,

        String horarioAtencion,

        BigDecimal latitud,

        BigDecimal longitud,

        Boolean esPrincipal,

        Boolean activo
) {}
