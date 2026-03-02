package org.example.servermanager.repository;

import org.example.servermanager.entity.Empresa;
import org.example.servermanager.enums.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByRuc(String ruc);

    Optional<Empresa> findByEmail(String email);

    List<Empresa> findByActivoTrue();

    List<Empresa> findByPlan(Plan plan);

    boolean existsByRuc(String ruc);

    boolean existsByEmail(String email);

    @Query("SELECT e FROM Empresa e LEFT JOIN FETCH e.configuracionBot WHERE e.id = :id")
    Optional<Empresa> findByIdWithConfiguracion(@Param("id") Long id);

    @Query("SELECT e FROM Empresa e WHERE e.activo = true AND e.configuracionBot.activo = true")
    List<Empresa> findEmpresasConBotActivo();

    @Query("SELECT COUNT(e) FROM Empresa e WHERE e.activo = true")
    long countActivas();
}
