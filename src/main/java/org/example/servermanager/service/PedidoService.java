package org.example.servermanager.service;

import org.example.servermanager.entity.DetallePedido;
import org.example.servermanager.entity.Pedido;
import org.example.servermanager.enums.EstadoPedido;
import org.example.servermanager.enums.MetodoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoService {

    Pedido crear(Long empresaId, Long conversacionId, Pedido pedido);

    Pedido agregarDetalle(Long pedidoId, DetallePedido detalle);

    Pedido actualizar(Long empresaId, Long pedidoId, Pedido pedido);

    Optional<Pedido> obtenerPorId(Long empresaId, Long pedidoId);

    Optional<Pedido> obtenerPorIdConDetalles(Long pedidoId);

    Optional<Pedido> buscarPedidoPendientePorMonto(Long empresaId, BigDecimal monto);

    List<Pedido> listarPorEmpresa(Long empresaId);

    Page<Pedido> listarPorEmpresaPaginado(Long empresaId, Pageable pageable);

    List<Pedido> listarPorEstado(Long empresaId, EstadoPedido estado);

    List<Pedido> listarPendientes(Long empresaId);

    List<Pedido> listarPorCliente(Long clienteId);

    List<Pedido> listarPorFechas(Long empresaId, LocalDateTime desde, LocalDateTime hasta);

    void cambiarEstado(Long pedidoId, EstadoPedido estado);

    void marcarComoPagado(Long pedidoId, MetodoPago metodoPago);

    void cancelar(Long empresaId, Long pedidoId, String motivo);

    BigDecimal generarMontoUnico(Long empresaId, BigDecimal montoBase);

    long contarPorEstado(Long empresaId, EstadoPedido estado);

    BigDecimal calcularVentasDesde(Long empresaId, LocalDateTime desde);
}
