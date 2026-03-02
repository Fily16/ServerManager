package org.example.servermanager.repository;

import org.example.servermanager.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEmpresaId(Long empresaId);

    List<Producto> findByEmpresaIdAndActivoTrue(Long empresaId);

    Page<Producto> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    Optional<Producto> findByIdAndEmpresaId(Long id, Long empresaId);

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);

    List<Producto> findByEmpresaIdAndCategoriaId(Long empresaId, Long categoriaId);

    List<Producto> findByEmpresaIdAndEsDestacadoTrue(Long empresaId);

    List<Producto> findByEmpresaIdAndEsDestacadoTrueAndActivoTrue(Long empresaId);

    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId AND p.activo = true " +
           "AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Producto> buscarPorTexto(@Param("empresaId") Long empresaId, @Param("busqueda") String busqueda);

    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId AND p.activo = true " +
           "AND p.precio BETWEEN :precioMin AND :precioMax")
    List<Producto> findByRangoPrecio(@Param("empresaId") Long empresaId, 
                                      @Param("precioMin") BigDecimal precioMin, 
                                      @Param("precioMax") BigDecimal precioMax);

    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId AND p.tieneStock = true AND p.stockActual > 0")
    List<Producto> findConStock(@Param("empresaId") Long empresaId);

    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId AND p.tieneStock = true AND p.stockActual <= :minimo")
    List<Producto> findConStockBajo(@Param("empresaId") Long empresaId, @Param("minimo") Integer minimo);

    long countByEmpresaId(Long empresaId);

    long countByEmpresaIdAndActivoTrue(Long empresaId);

    boolean existsByIdAndEmpresaId(Long id, Long empresaId);
}
