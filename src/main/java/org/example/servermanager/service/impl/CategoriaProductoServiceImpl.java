package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.CategoriaProducto;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.exception.DuplicateResourceException;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.CategoriaProductoRepository;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.service.CategoriaProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaProductoServiceImpl implements CategoriaProductoService {

    private final CategoriaProductoRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public CategoriaProducto crear(Long empresaId, CategoriaProducto categoria) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        if (categoriaRepository.existsByEmpresaIdAndNombre(empresaId, categoria.getNombre())) {
            throw new DuplicateResourceException("CategoriaProducto", "nombre", categoria.getNombre());
        }

        categoria.setEmpresa(empresa);
        
        if (categoria.getOrden() == null || categoria.getOrden() == 0) {
            int maxOrden = categoriaRepository.findMaxOrdenByEmpresaId(empresaId);
            categoria.setOrden(maxOrden + 1);
        }

        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public CategoriaProducto actualizar(Long empresaId, Long categoriaId, CategoriaProducto categoria) {
        CategoriaProducto existente = categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", "id", categoriaId));

        existente.setNombre(categoria.getNombre());
        existente.setDescripcion(categoria.getDescripcion());
        
        if (categoria.getOrden() != null) {
            existente.setOrden(categoria.getOrden());
        }

        return categoriaRepository.save(existente);
    }

    @Override
    public Optional<CategoriaProducto> obtenerPorId(Long empresaId, Long categoriaId) {
        return categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId);
    }

    @Override
    public Optional<CategoriaProducto> obtenerPorIdConProductos(Long categoriaId) {
        return categoriaRepository.findByIdWithProductos(categoriaId);
    }

    @Override
    public List<CategoriaProducto> listarPorEmpresa(Long empresaId) {
        return categoriaRepository.findByEmpresaIdOrderByOrdenAsc(empresaId);
    }

    @Override
    public List<CategoriaProducto> listarActivasPorEmpresa(Long empresaId) {
        return categoriaRepository.findByEmpresaIdAndActivoTrueOrderByOrdenAsc(empresaId);
    }

    @Override
    @Transactional
    public void reordenar(Long empresaId, Long categoriaId, Integer nuevoOrden) {
        categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId)
                .ifPresent(categoria -> {
                    categoria.setOrden(nuevoOrden);
                    categoriaRepository.save(categoria);
                });
    }

    @Override
    @Transactional
    public void activar(Long empresaId, Long categoriaId) {
        categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId).ifPresent(cat -> {
            cat.setActivo(true);
            categoriaRepository.save(cat);
        });
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long categoriaId) {
        categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId).ifPresent(cat -> {
            cat.setActivo(false);
            categoriaRepository.save(cat);
        });
    }

    @Override
    @Transactional
    public void eliminar(Long empresaId, Long categoriaId) {
        categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId)
                .ifPresent(categoriaRepository::delete);
    }
}
