package org.example.servermanager.repository;

import org.example.servermanager.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedidoId(Long pedidoId);

    List<DetallePedido> findByProductoId(Long productoId);

    @Query("SELECT d.nombreProducto, SUM(d.cantidad), SUM(d.subtotal) FROM DetallePedido d " +
           "JOIN d.pedido p WHERE p.empresa.id = :empresaId AND p.fechaCreacion >= :desde " +
           "AND p.estado != 'CANCELADO' GROUP BY d.nombreProducto ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> getProductosMasVendidos(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    @Query("SELECT COUNT(d) FROM DetallePedido d WHERE d.pedido.id = :pedidoId")
    long countByPedidoId(@Param("pedidoId") Long pedidoId);

    @Query("SELECT SUM(d.cantidad) FROM DetallePedido d WHERE d.producto.id = :productoId " +
           "AND d.pedido.estado != 'CANCELADO'")
    Long sumCantidadVendidaPorProducto(@Param("productoId") Long productoId);
}
