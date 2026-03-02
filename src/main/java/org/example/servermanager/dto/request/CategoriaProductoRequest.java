package org.example.servermanager.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String descripcion,

        @Min(value = 0, message = "El orden debe ser mayor o igual a 0")
        Integer orden,

        Boolean activo
) {}
