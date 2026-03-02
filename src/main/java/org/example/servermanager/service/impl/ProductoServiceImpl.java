package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.entity.Producto;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.repository.ProductoRepository;
import org.example.servermanager.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public Producto crear(Long empresaId, Producto producto) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        producto.setEmpresa(empresa);
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizar(Long empresaId, Long productoId, Producto producto) {
        Producto existente = productoRepository.findByIdAndEmpresaId(productoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));

        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecio(producto.getPrecio());
        existente.setPrecioOferta(producto.getPrecioOferta());
        existente.setImagenUrl(producto.getImagenUrl());
        existente.setTieneStock(producto.getTieneStock());
        existente.setStockActual(producto.getStockActual());
        existente.setEsDestacado(producto.getEsDestacado());
        existente.setTags(producto.getTags());
        existente.setCategoria(producto.getCategoria());

        return productoRepository.save(existente);
    }

    @Override
    public Optional<Producto> obtenerPorId(Long empresaId, Long productoId) {
        return productoRepository.findByIdAndEmpresaId(productoId, empresaId);
    }

    @Override
    public List<Producto> listarPorEmpresa(Long empresaId) {
        return productoRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    @Override
    public Page<Producto> listarPorEmpresaPaginado(Long empresaId, Pageable pageable) {
        return productoRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
    }

    @Override
    public List<Producto> listarPorCategoria(Long empresaId, Long categoriaId) {
        return productoRepository.findByEmpresaIdAndCategoriaId(empresaId, categoriaId);
    }

    @Override
    public List<Producto> listarDestacados(Long empresaId) {
        return productoRepository.findByEmpresaIdAndEsDestacadoTrueAndActivoTrue(empresaId);
    }

    @Override
    public List<Producto> buscar(Long empresaId, String busqueda) {
        return productoRepository.buscarPorTexto(empresaId, busqueda);
    }

    @Override
    public List<Producto> buscarPorRangoPrecio(Long empresaId, BigDecimal precioMin, BigDecimal precioMax) {
        return productoRepository.findByRangoPrecio(empresaId, precioMin, precioMax);
    }

    @Override
    public List<Producto> obtenerConStockBajo(Long empresaId, Integer minimoStock) {
        return productoRepository.findConStockBajo(empresaId, minimoStock);
    }

    @Override
    @Transactional
    public void actualizarStock(Long empresaId, Long productoId, Integer cantidad) {
        productoRepository.findByIdAndEmpresaId(productoId, empresaId).ifPresent(producto -> {
            producto.setStockActual(producto.getStockActual() + cantidad);
            productoRepository.save(producto);
        });
    }

    @Override
    @Transactional
    public void activar(Long empresaId, Long productoId) {
        productoRepository.findByIdAndEmpresaId(productoId, empresaId).ifPresent(producto -> {
            producto.setActivo(true);
            productoRepository.save(producto);
        });
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long productoId) {
        productoRepository.findByIdAndEmpresaId(productoId, empresaId).ifPresent(producto -> {
            producto.setActivo(false);
            productoRepository.save(producto);
        });
    }

    @Override
    @Transactional
    public void eliminar(Long empresaId, Long productoId) {
        productoRepository.findByIdAndEmpresaId(productoId, empresaId)
                .ifPresent(productoRepository::delete);
    }

    @Override
    public long contarPorEmpresa(Long empresaId) {
        return productoRepository.countByEmpresaId(empresaId);
    }
}
