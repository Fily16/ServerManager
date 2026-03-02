package org.example.servermanager.repository;

import org.example.servermanager.entity.TecnicaVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TecnicaVentaRepository extends JpaRepository<TecnicaVenta, Long> {

    List<TecnicaVenta> findByActivoTrueOrderByPrioridadAsc();

    List<TecnicaVenta> findByCategoriaAndActivoTrueOrderByPrioridadAsc(String categoria);

    @Query("SELECT t FROM TecnicaVenta t WHERE t.activo = true ORDER BY t.prioridad ASC")
    List<TecnicaVenta> findAllActivas();

    @Query("SELECT DISTINCT t.categoria FROM TecnicaVenta t WHERE t.activo = true")
    List<String> findCategoriasActivas();
}
