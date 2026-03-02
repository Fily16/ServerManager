package org.example.servermanager.repository;

import org.example.servermanager.entity.PagoDetectado;
import org.example.servermanager.enums.Plataforma;
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
public interface PagoDetectadoRepository extends JpaRepository<PagoDetectado, Long> {

    List<PagoDetectado> findByEmpresaId(Long empresaId);

    Page<PagoDetectado> findByEmpresaId(Long empresaId, Pageable pageable);

    List<PagoDetectado> findByEmpresaIdAndProcesadoFalse(Long empresaId);

    Optional<PagoDetectado> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<PagoDetectado> findByPedidoId(Long pedidoId);

    @Query("SELECT p FROM PagoDetectado p WHERE p.empresa.id = :empresaId AND p.monto = :monto " +
           "AND p.procesado = false AND p.fechaDeteccion >= :desde ORDER BY p.fechaDeteccion DESC")
    Optional<PagoDetectado> findPagoPorMonto(@Param("empresaId") Long empresaId, 
                                              @Param("monto") BigDecimal monto,
                                              @Param("desde") LocalDateTime desde);

    @Query("SELECT p FROM PagoDetectado p WHERE p.empresa.id = :empresaId " +
           "AND p.fechaDeteccion BETWEEN :desde AND :hasta ORDER BY p.fechaDeteccion DESC")
    List<PagoDetectado> findByEmpresaIdAndFechaEntre(@Param("empresaId") Long empresaId,
                                                      @Param("desde") LocalDateTime desde,
                                                      @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(p) FROM PagoDetectado p WHERE p.empresa.id = :empresaId AND p.procesado = false")
    long countNoProcesados(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(p) FROM PagoDetectado p WHERE p.empresa.id = :empresaId AND p.matchAutomatico = true " +
           "AND p.fechaDeteccion >= :desde")
    long countMatchAutomaticoDesde(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    @Query("SELECT p.plataforma, COUNT(p), COALESCE(SUM(p.monto), 0) FROM PagoDetectado p " +
           "WHERE p.empresa.id = :empresaId AND p.fechaDeteccion >= :desde GROUP BY p.plataforma")
    List<Object[]> getEstadisticasPorPlataforma(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    boolean existsByReferenciaExterna(String referenciaExterna);
}
