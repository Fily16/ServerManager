package org.example.servermanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.servermanager.enums.Plan;

public record EmpresaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String nombre,

        @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener 11 dígitos")
        String ruc,

        @Email(message = "El email debe ser válido")
        String email,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono,

        @Size(max = 500, message = "La URL del logo no puede exceder 500 caracteres")
        String logoUrl,

        @Size(max = 500, message = "La dirección fiscal no puede exceder 500 caracteres")
        String direccionFiscal,

        Plan plan
) {}
