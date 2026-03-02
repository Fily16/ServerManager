package org.example.servermanager.dto.response;

import org.example.servermanager.entity.PagoDetectado;
import org.example.servermanager.enums.Plataforma;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoDetectadoResponse(
        Long id,
        Long empresaId,
        Long pedidoId,
        String nombreCliente,
        BigDecimal monto,
        Plataforma plataforma,
        String referenciaExterna,
        Boolean procesado,
        Boolean matchAutomatico,
        LocalDateTime fechaDeteccion
) {
    public static PagoDetectadoResponse fromEntity(PagoDetectado pago) {
        return new PagoDetectadoResponse(
                pago.getId(),
                pago.getEmpresa().getId(),
                pago.getPedido() != null ? pago.getPedido().getId() : null,
                pago.getNombreCliente(),
                pago.getMonto(),
                pago.getPlataforma(),
                pago.getReferenciaExterna(),
                pago.getProcesado(),
                pago.getMatchAutomatico(),
                pago.getFechaDeteccion()
        );
    }
}
