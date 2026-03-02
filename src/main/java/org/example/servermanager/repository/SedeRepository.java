package org.example.servermanager.repository;

import org.example.servermanager.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {

    List<Sede> findByEmpresaId(Long empresaId);

    List<Sede> findByEmpresaIdAndActivoTrue(Long empresaId);

    Optional<Sede> findByEmpresaIdAndEsPrincipalTrue(Long empresaId);

    Optional<Sede> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT s FROM Sede s WHERE s.empresa.id = :empresaId ORDER BY s.esPrincipal DESC, s.nombre ASC")
    List<Sede> findByEmpresaIdOrdenadas(@Param("empresaId") Long empresaId);

    long countByEmpresaId(Long empresaId);

    boolean existsByIdAndEmpresaId(Long id, Long empresaId);
}
