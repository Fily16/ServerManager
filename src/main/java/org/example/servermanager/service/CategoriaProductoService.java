package org.example.servermanager.service;

import org.example.servermanager.entity.CategoriaProducto;

import java.util.List;
import java.util.Optional;

public interface CategoriaProductoService {

    CategoriaProducto crear(Long empresaId, CategoriaProducto categoria);

    CategoriaProducto actualizar(Long empresaId, Long categoriaId, CategoriaProducto categoria);

    Optional<CategoriaProducto> obtenerPorId(Long empresaId, Long categoriaId);

    Optional<CategoriaProducto> obtenerPorIdConProductos(Long categoriaId);

    List<CategoriaProducto> listarPorEmpresa(Long empresaId);

    List<CategoriaProducto> listarActivasPorEmpresa(Long empresaId);

    void reordenar(Long empresaId, Long categoriaId, Integer nuevoOrden);

    void activar(Long empresaId, Long categoriaId);

    void desactivar(Long empresaId, Long categoriaId);

    void eliminar(Long empresaId, Long categoriaId);
}
