package org.example.servermanager.dto.response;

import org.example.servermanager.entity.DetallePedido;

import java.math.BigDecimal;

public record DetallePedidoResponse(
        Long id,
        Long productoId,
        String nombreProducto,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal subtotal,
        String notas
) {
    public static DetallePedidoResponse fromEntity(DetallePedido detalle) {
        return new DetallePedidoResponse(
                detalle.getId(),
                detalle.getProducto() != null ? detalle.getProducto().getId() : null,
                detalle.getNombreProducto(),
                detalle.getPrecioUnitario(),
                detalle.getCantidad(),
                detalle.getSubtotal(),
                detalle.getNotas()
        );
    }
}
