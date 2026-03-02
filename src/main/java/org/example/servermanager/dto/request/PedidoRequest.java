package org.example.servermanager.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.servermanager.enums.MetodoPago;

import java.math.BigDecimal;
import java.util.List;

public record PedidoRequest(
        Long conversacionId,

        Long clienteId,

        @NotBlank(message = "El teléfono del cliente es obligatorio")
        String telefonoCliente,

        String nombreCliente,

        @Size(max = 500, message = "La dirección de envío no puede exceder 500 caracteres")
        String direccionEnvio,

        @DecimalMin(value = "0.00", message = "El costo de envío no puede ser negativo")
        BigDecimal costoEnvio,

        @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
        BigDecimal descuento,

        MetodoPago metodoPago,

        String notas,

        @Valid
        List<DetallePedidoRequest> detalles
) {}
