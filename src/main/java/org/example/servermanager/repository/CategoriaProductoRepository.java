package org.example.servermanager.repository;

import org.example.servermanager.entity.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long> {

    List<CategoriaProducto> findByEmpresaId(Long empresaId);

    List<CategoriaProducto> findByEmpresaIdAndActivoTrue(Long empresaId);

    List<CategoriaProducto> findByEmpresaIdOrderByOrdenAsc(Long empresaId);

    List<CategoriaProducto> findByEmpresaIdAndActivoTrueOrderByOrdenAsc(Long empresaId);

    @Query("SELECT COALESCE(MAX(c.orden), 0) FROM CategoriaProducto c WHERE c.empresa.id = :empresaId")
    int findMaxOrdenByEmpresaId(@Param("empresaId") Long empresaId);

    Optional<CategoriaProducto> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<CategoriaProducto> findByEmpresaIdAndNombre(Long empresaId, String nombre);

    @Query("SELECT c FROM CategoriaProducto c LEFT JOIN FETCH c.productos WHERE c.id = :id")
    Optional<CategoriaProducto> findByIdWithProductos(@Param("id") Long id);

    boolean existsByEmpresaIdAndNombre(Long empresaId, String nombre);

    long countByEmpresaId(Long empresaId);
}
