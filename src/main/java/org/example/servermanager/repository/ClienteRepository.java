package org.example.servermanager.repository;

import org.example.servermanager.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByEmpresaId(Long empresaId);

    Page<Cliente> findByEmpresaId(Long empresaId, Pageable pageable);

    Optional<Cliente> findByEmpresaIdAndTelefono(Long empresaId, String telefono);

    Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR c.telefono LIKE CONCAT('%', :busqueda, '%') " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Cliente> buscarPorTexto(@Param("empresaId") Long empresaId, @Param("busqueda") String busqueda);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId ORDER BY c.totalPedidos DESC")
    List<Cliente> findTopClientesPorPedidos(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND c.ultimaInteraccion >= :desde")
    List<Cliente> findClientesActivos(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND c.ultimaInteraccion < :desde")
    List<Cliente> findClientesInactivos(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    boolean existsByEmpresaIdAndTelefono(Long empresaId, String telefono);

    long countByEmpresaId(Long empresaId);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.empresa.id = :empresaId AND c.ultimaInteraccion >= :desde")
    long countClientesActivos(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);
}
