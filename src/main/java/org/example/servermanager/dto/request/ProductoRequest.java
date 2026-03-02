package org.example.servermanager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String nombre,

        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        BigDecimal precio,

        @DecimalMin(value = "0.00", message = "El precio de oferta no puede ser negativo")
        BigDecimal precioOferta,

        @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
        String imagenUrl,

        Long categoriaId,

        Boolean tieneStock,

        Integer stockActual,

        Boolean esDestacado,

        @Size(max = 500, message = "Los tags no pueden exceder 500 caracteres")
        String tags,

        Boolean activo
) {}
