package org.example.servermanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9]{9,15}$", message = "El teléfono debe tener entre 9 y 15 dígitos")
        String telefono,

        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String nombre,

        @Email(message = "El email debe ser válido")
        String email,

        @Size(max = 100, message = "El distrito no puede exceder 100 caracteres")
        String distrito,

        @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
        String direccion
) {}
