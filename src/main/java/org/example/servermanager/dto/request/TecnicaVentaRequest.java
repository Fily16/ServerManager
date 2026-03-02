package org.example.servermanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TecnicaVentaRequest(
        @NotBlank(message = "La categoría es requerida")
        String categoria,

        @NotBlank(message = "El nombre es requerido")
        @Size(max = 200)
        String nombre,

        @NotBlank(message = "La descripción es requerida")
        String descripcion,

        String ejemplo,

        @Size(max = 200)
        String fuente,

        Integer prioridad
) {}
