package org.example.servermanager.repository;

import org.example.servermanager.entity.Campana;
import org.example.servermanager.enums.EstadoCampana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampanaRepository extends JpaRepository<Campana, Long> {

    List<Campana> findByEmpresaIdOrderByFechaCreacionDesc(Long empresaId);

    List<Campana> findByEmpresaIdAndEstado(Long empresaId, EstadoCampana estado);

    /** Busca campanas activas (en progreso) para el scheduler */
    List<Campana> findByEstado(EstadoCampana estado);
}
