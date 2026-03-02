package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.Conversacion;
import org.example.servermanager.entity.DetallePedido;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.entity.Pedido;
import org.example.servermanager.enums.EstadoConversacion;
import org.example.servermanager.enums.EstadoPedido;
import org.example.servermanager.enums.MetodoPago;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.ConversacionRepository;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.repository.PedidoRepository;
import org.example.servermanager.service.ClienteService;
import org.example.servermanager.service.PedidoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EmpresaRepository empresaRepository;
    private final ConversacionRepository conversacionRepository;
    private final ClienteService clienteService;

    private static final int MINUTOS_VIGENCIA_PAGO = 30;

    @Override
    @Transactional
    public Pedido crear(Long empresaId, Long conversacionId, Pedido pedido) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        pedido.setEmpresa(empresa);

        if (conversacionId != null) {
            Conversacion conversacion = conversacionRepository.findById(conversacionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversación", "id", conversacionId));
            pedido.setConversacion(conversacion);
            pedido.setCliente(conversacion.getCliente());
            
            conversacion.setEstado(EstadoConversacion.ESPERANDO_PAGO);
            conversacionRepository.save(conversacion);
        }

        pedido.calcularTotal();
        BigDecimal montoUnico = generarMontoUnico(empresaId, pedido.getTotal());
        pedido.setMontoUnico(montoUnico);

        if (pedido.getCliente() != null) {
            clienteService.incrementarPedidos(pedido.getCliente().getId());
        }

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido agregarDetalle(Long pedidoId, DetallePedido detalle) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        pedido.addDetalle(detalle);
        pedido.calcularTotal();
        pedido.setMontoUnico(generarMontoUnico(pedido.getEmpresa().getId(), pedido.getTotal()));

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido actualizar(Long empresaId, Long pedidoId, Pedido pedido) {
        Pedido existente = pedidoRepository.findByIdAndEmpresaId(pedidoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", pedidoId));

        existente.setNombreCliente(pedido.getNombreCliente());
        existente.setDireccionEnvio(pedido.getDireccionEnvio());
        existente.setCostoEnvio(pedido.getCostoEnvio());
        existente.setDescuento(pedido.getDescuento());
        existente.setNotas(pedido.getNotas());
        existente.calcularTotal();

        return pedidoRepository.save(existente);
    }

    @Override
    public Optional<Pedido> obtenerPorId(Long empresaId, Long pedidoId) {
        return pedidoRepository.findByIdAndEmpresaId(pedidoId, empresaId);
    }

    @Override
    public Optional<Pedido> obtenerPorIdConDetalles(Long pedidoId) {
        return pedidoRepository.findByIdWithDetalles(pedidoId);
    }

    @Override
    public Optional<Pedido> buscarPedidoPendientePorMonto(Long empresaId, BigDecimal monto) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(MINUTOS_VIGENCIA_PAGO);
        return pedidoRepository.findPedidoPendientePorMonto(empresaId, monto, desde);
    }

    @Override
    public List<Pedido> listarPorEmpresa(Long empresaId) {
        return pedidoRepository.findByEmpresaId(empresaId);
    }

    @Override
    public Page<Pedido> listarPorEmpresaPaginado(Long empresaId, Pageable pageable) {
        return pedidoRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    public List<Pedido> listarPorEstado(Long empresaId, EstadoPedido estado) {
        return pedidoRepository.findByEmpresaIdAndEstado(empresaId, estado);
    }

    @Override
    public List<Pedido> listarPendientes(Long empresaId) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(MINUTOS_VIGENCIA_PAGO);
        return pedidoRepository.findPedidosPendientes(empresaId, desde);
    }

    @Override
    public List<Pedido> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Override
    public List<Pedido> listarPorFechas(Long empresaId, LocalDateTime desde, LocalDateTime hasta) {
        return pedidoRepository.findByEmpresaIdAndFechaEntre(empresaId, desde, hasta);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long pedidoId, EstadoPedido estado) {
        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            pedido.setEstado(estado);
            
            switch (estado) {
                case ENVIADO -> pedido.setFechaEnvio(LocalDateTime.now());
                case ENTREGADO -> pedido.setFechaEntrega(LocalDateTime.now());
            }
            
            pedidoRepository.save(pedido);
        });
    }

    @Override
    @Transactional
    public void marcarComoPagado(Long pedidoId, MetodoPago metodoPago) {
        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            pedido.marcarComoPagado(metodoPago);
            
            if (pedido.getConversacion() != null) {
                Conversacion conversacion = pedido.getConversacion();
                conversacion.setEstado(EstadoConversacion.ACTIVA);
                conversacionRepository.save(conversacion);
            }
            
            pedidoRepository.save(pedido);
        });
    }

    @Override
    @Transactional
    public void cancelar(Long empresaId, Long pedidoId, String motivo) {
        pedidoRepository.findByIdAndEmpresaId(pedidoId, empresaId).ifPresent(pedido -> {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedido.setNotas((pedido.getNotas() != null ? pedido.getNotas() + " | " : "") + "Cancelado: " + motivo);
            pedidoRepository.save(pedido);
        });
    }

    @Override
    public BigDecimal generarMontoUnico(Long empresaId, BigDecimal montoBase) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(MINUTOS_VIGENCIA_PAGO);

        for (int intento = 0; intento < 10; intento++) {
            int centavos = ThreadLocalRandom.current().nextInt(1, 100);
            BigDecimal montoUnico = montoBase.setScale(0, java.math.RoundingMode.DOWN)
                    .add(new BigDecimal("0." + String.format("%02d", centavos)));

            if (!pedidoRepository.existsMontoUnicoVigente(empresaId, montoUnico, EstadoPedido.PENDIENTE_PAGO, desde)) {
                return montoUnico;
            }
        }

        long millis = System.currentTimeMillis() % 100;
        return montoBase.setScale(0, java.math.RoundingMode.DOWN)
                .add(new BigDecimal("0." + String.format("%02d", millis)));
    }

    @Override
    public long contarPorEstado(Long empresaId, EstadoPedido estado) {
        return pedidoRepository.countByEmpresaIdAndEstado(empresaId, estado);
    }

    @Override
    public BigDecimal calcularVentasDesde(Long empresaId, LocalDateTime desde) {
        return pedidoRepository.sumVentasDesde(empresaId, desde);
    }
}
