package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.entity.PagoDetectado;
import org.example.servermanager.entity.Pedido;
import org.example.servermanager.enums.MetodoPago;
import org.example.servermanager.enums.Plataforma;
import org.example.servermanager.exception.DuplicateResourceException;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.repository.PagoDetectadoRepository;
import org.example.servermanager.repository.PedidoRepository;
import org.example.servermanager.service.PagoDetectadoService;
import org.example.servermanager.service.PedidoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoDetectadoServiceImpl implements PagoDetectadoService {

    private final PagoDetectadoRepository pagoDetectadoRepository;
    private final PedidoRepository pedidoRepository;
    private final EmpresaRepository empresaRepository;
    private final PedidoService pedidoService;

    private static final int MINUTOS_VIGENCIA_PAGO = 30;

    @Override
    @Transactional
    public PagoDetectado registrar(Long empresaId, BigDecimal monto, String nombreCliente, Plataforma plataforma, String referencia) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        if (referencia != null && pagoDetectadoRepository.existsByReferenciaExterna(referencia)) {
            throw new DuplicateResourceException("PagoDetectado", "referenciaExterna", referencia);
        }

        PagoDetectado pago = PagoDetectado.builder()
                .empresa(empresa)
                .monto(monto)
                .nombreCliente(nombreCliente)
                .plataforma(plataforma)
                .referenciaExterna(referencia)
                .procesado(false)
                .matchAutomatico(false)
                .build();

        return pagoDetectadoRepository.save(pago);
    }

    @Override
    @Transactional
    public PagoDetectado procesarYAsociar(Long empresaId, BigDecimal monto, String nombreCliente, Plataforma plataforma) {
        PagoDetectado pago = registrar(empresaId, monto, nombreCliente, plataforma, null);

        Optional<Pedido> pedidoOpt = pedidoService.buscarPedidoPendientePorMonto(empresaId, monto);

        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            pago.asociarPedido(pedido);
            pagoDetectadoRepository.save(pago);

            MetodoPago metodoPago = plataforma == Plataforma.YAPE ? MetodoPago.YAPE : MetodoPago.PLIN;
            pedidoService.marcarComoPagado(pedido.getId(), metodoPago);
        }

        return pago;
    }

    @Override
    public Optional<PagoDetectado> obtenerPorId(Long empresaId, Long pagoId) {
        return pagoDetectadoRepository.findByIdAndEmpresaId(pagoId, empresaId);
    }

    @Override
    public Optional<PagoDetectado> obtenerPorPedidoId(Long pedidoId) {
        return pagoDetectadoRepository.findByPedidoId(pedidoId);
    }

    @Override
    public List<PagoDetectado> listarPorEmpresa(Long empresaId) {
        return pagoDetectadoRepository.findByEmpresaId(empresaId);
    }

    @Override
    public Page<PagoDetectado> listarPorEmpresaPaginado(Long empresaId, Pageable pageable) {
        return pagoDetectadoRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    public List<PagoDetectado> listarNoProcesados(Long empresaId) {
        return pagoDetectadoRepository.findByEmpresaIdAndProcesadoFalse(empresaId);
    }

    @Override
    public List<PagoDetectado> listarPorFechas(Long empresaId, LocalDateTime desde, LocalDateTime hasta) {
        return pagoDetectadoRepository.findByEmpresaIdAndFechaEntre(empresaId, desde, hasta);
    }

    @Override
    @Transactional
    public void asociarAPedido(Long pagoId, Long pedidoId) {
        PagoDetectado pago = pagoDetectadoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("PagoDetectado", "id", pagoId));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        pago.setPedido(pedido);
        pago.setProcesado(true);
        pagoDetectadoRepository.save(pago);

        MetodoPago metodoPago = pago.getPlataforma() == Plataforma.YAPE ? MetodoPago.YAPE : MetodoPago.PLIN;
        pedidoService.marcarComoPagado(pedidoId, metodoPago);
    }

    @Override
    @Transactional
    public void marcarComoProcesado(Long pagoId) {
        pagoDetectadoRepository.findById(pagoId).ifPresent(pago -> {
            pago.setProcesado(true);
            pagoDetectadoRepository.save(pago);
        });
    }

    @Override
    public long contarNoProcesados(Long empresaId) {
        return pagoDetectadoRepository.countNoProcesados(empresaId);
    }

    @Override
    public long contarMatchAutomaticoDesde(Long empresaId, LocalDateTime desde) {
        return pagoDetectadoRepository.countMatchAutomaticoDesde(empresaId, desde);
    }
}
