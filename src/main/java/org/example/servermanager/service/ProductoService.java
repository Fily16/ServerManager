package org.example.servermanager.service;

import org.example.servermanager.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoService {

    Producto crear(Long empresaId, Producto producto);

    Producto actualizar(Long empresaId, Long productoId, Producto producto);

    Optional<Producto> obtenerPorId(Long empresaId, Long productoId);

    List<Producto> listarPorEmpresa(Long empresaId);

    Page<Producto> listarPorEmpresaPaginado(Long empresaId, Pageable pageable);

    List<Producto> listarPorCategoria(Long empresaId, Long categoriaId);

    List<Producto> listarDestacados(Long empresaId);

    List<Producto> buscar(Long empresaId, String busqueda);

    List<Producto> buscarPorRangoPrecio(Long empresaId, BigDecimal precioMin, BigDecimal precioMax);

    List<Producto> obtenerConStockBajo(Long empresaId, Integer minimoStock);

    void actualizarStock(Long empresaId, Long productoId, Integer cantidad);

    void activar(Long empresaId, Long productoId);

    void desactivar(Long empresaId, Long productoId);

    void eliminar(Long empresaId, Long productoId);

    long contarPorEmpresa(Long empresaId);
}
