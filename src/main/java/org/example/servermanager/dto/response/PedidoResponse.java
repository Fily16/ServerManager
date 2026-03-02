package org.example.servermanager.dto.response;

import org.example.servermanager.entity.Pedido;
import org.example.servermanager.enums.EstadoPedido;
import org.example.servermanager.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long empresaId,
        Long conversacionId,
        Long clienteId,
        String telefonoCliente,
        String nombreCliente,
        String direccionEnvio,
        BigDecimal subtotal,
        BigDecimal costoEnvio,
        BigDecimal descuento,
        BigDecimal total,
        BigDecimal montoUnico,
        EstadoPedido estado,
        MetodoPago metodoPago,
        String notas,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaPago,
        LocalDateTime fechaEnvio,
        LocalDateTime fechaEntrega,
        List<DetallePedidoResponse> detalles
) {
    public static PedidoResponse fromEntity(Pedido pedido) {
        List<DetallePedidoResponse> detallesResponse = pedido.getDetalles() != null
                ? pedido.getDetalles().stream().map(DetallePedidoResponse::fromEntity).toList()
                : List.of();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getEmpresa().getId(),
                pedido.getConversacion() != null ? pedido.getConversacion().getId() : null,
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getTelefonoCliente(),
                pedido.getNombreCliente(),
                pedido.getDireccionEnvio(),
                pedido.getSubtotal(),
                pedido.getCostoEnvio(),
                pedido.getDescuento(),
                pedido.getTotal(),
                pedido.getMontoUnico(),
                pedido.getEstado(),
                pedido.getMetodoPago(),
                pedido.getNotas(),
                pedido.getFechaCreacion(),
                pedido.getFechaPago(),
                pedido.getFechaEnvio(),
                pedido.getFechaEntrega(),
                detallesResponse
        );
    }
}
