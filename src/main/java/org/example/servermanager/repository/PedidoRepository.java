package org.example.servermanager.repository;

import org.example.servermanager.entity.Pedido;
import org.example.servermanager.enums.EstadoPedido;
import org.example.servermanager.enums.MetodoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEmpresaId(Long empresaId);

    Page<Pedido> findByEmpresaId(Long empresaId, Pageable pageable);

    List<Pedido> findByEmpresaIdAndEstado(Long empresaId, EstadoPedido estado);

    Optional<Pedido> findByIdAndEmpresaId(Long id, Long empresaId);

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByConversacionId(Long conversacionId);

    @Query("SELECT p FROM Pedido p WHERE p.empresa.id = :empresaId AND p.montoUnico = :monto " +
           "AND p.estado = 'PENDIENTE_PAGO' AND p.fechaCreacion >= :desde ORDER BY p.fechaCreacion DESC")
    Optional<Pedido> findPedidoPendientePorMonto(@Param("empresaId") Long empresaId, 
                                                  @Param("monto") BigDecimal monto,
                                                  @Param("desde") LocalDateTime desde);

    @Query("SELECT p FROM Pedido p WHERE p.empresa.id = :empresaId AND p.estado = 'PENDIENTE_PAGO' " +
           "AND p.fechaCreacion >= :desde ORDER BY p.fechaCreacion DESC")
    List<Pedido> findPedidosPendientes(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles WHERE p.id = :id")
    Optional<Pedido> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT p FROM Pedido p WHERE p.empresa.id = :empresaId " +
           "AND p.fechaCreacion BETWEEN :desde AND :hasta ORDER BY p.fechaCreacion DESC")
    List<Pedido> findByEmpresaIdAndFechaEntre(@Param("empresaId") Long empresaId, 
                                               @Param("desde") LocalDateTime desde,
                                               @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.empresa.id = :empresaId AND p.estado = :estado")
    long countByEmpresaIdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") EstadoPedido estado);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.empresa.id = :empresaId " +
           "AND p.estado IN ('PAGADO', 'CONFIRMADO', 'EN_PREPARACION', 'ENVIADO', 'ENTREGADO') " +
           "AND p.fechaPago >= :desde")
    BigDecimal sumVentasDesde(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    @Query("SELECT p.metodoPago, COUNT(p), COALESCE(SUM(p.total), 0) FROM Pedido p " +
           "WHERE p.empresa.id = :empresaId AND p.estado != 'CANCELADO' AND p.fechaCreacion >= :desde " +
           "GROUP BY p.metodoPago")
    List<Object[]> getEstadisticasPorMetodoPago(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    boolean existsByEmpresaIdAndMontoUnicoAndEstado(Long empresaId, BigDecimal montoUnico, EstadoPedido estado);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Pedido p " +
           "WHERE p.empresa.id = :empresaId AND p.montoUnico = :montoUnico " +
           "AND p.estado = :estado AND p.fechaCreacion >= :desde")
    boolean existsMontoUnicoVigente(@Param("empresaId") Long empresaId,
                                     @Param("montoUnico") BigDecimal montoUnico,
                                     @Param("estado") EstadoPedido estado,
                                     @Param("desde") LocalDateTime desde);
}
