package org.example.servermanager.service;

import org.example.servermanager.entity.PagoDetectado;
import org.example.servermanager.enums.Plataforma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoDetectadoService {

    PagoDetectado registrar(Long empresaId, BigDecimal monto, String nombreCliente, Plataforma plataforma, String referencia);

    PagoDetectado procesarYAsociar(Long empresaId, BigDecimal monto, String nombreCliente, Plataforma plataforma);

    Optional<PagoDetectado> obtenerPorId(Long empresaId, Long pagoId);

    Optional<PagoDetectado> obtenerPorPedidoId(Long pedidoId);

    List<PagoDetectado> listarPorEmpresa(Long empresaId);

    Page<PagoDetectado> listarPorEmpresaPaginado(Long empresaId, Pageable pageable);

    List<PagoDetectado> listarNoProcesados(Long empresaId);

    List<PagoDetectado> listarPorFechas(Long empresaId, LocalDateTime desde, LocalDateTime hasta);

    void asociarAPedido(Long pagoId, Long pedidoId);

    void marcarComoProcesado(Long pagoId);

    long contarNoProcesados(Long empresaId);

    long contarMatchAutomaticoDesde(Long empresaId, LocalDateTime desde);
}
