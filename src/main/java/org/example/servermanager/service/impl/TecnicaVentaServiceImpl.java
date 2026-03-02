package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.request.TecnicaVentaRequest;
import org.example.servermanager.dto.response.TecnicaVentaResponse;
import org.example.servermanager.entity.TecnicaVenta;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.TecnicaVentaRepository;
import org.example.servermanager.service.TecnicaVentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TecnicaVentaServiceImpl implements TecnicaVentaService {

    private final TecnicaVentaRepository tecnicaVentaRepository;

    @Override
    public List<TecnicaVentaResponse> listarTodas() {
        return tecnicaVentaRepository.findAll().stream()
                .map(TecnicaVentaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TecnicaVentaResponse> listarActivas() {
        return tecnicaVentaRepository.findAllActivas().stream()
                .map(TecnicaVentaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TecnicaVentaResponse> listarPorCategoria(String categoria) {
        return tecnicaVentaRepository.findByCategoriaAndActivoTrueOrderByPrioridadAsc(categoria.toUpperCase())
                .stream()
                .map(TecnicaVentaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listarCategorias() {
        return tecnicaVentaRepository.findCategoriasActivas();
    }

    @Override
    public TecnicaVentaResponse obtenerPorId(Long id) {
        TecnicaVenta tecnica = tecnicaVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TecnicaVenta", "id", id));
        return TecnicaVentaResponse.fromEntity(tecnica);
    }

    @Override
    @Transactional
    public TecnicaVentaResponse crear(TecnicaVentaRequest request) {
        TecnicaVenta tecnica = TecnicaVenta.builder()
                .categoria(request.categoria().toUpperCase())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .ejemplo(request.ejemplo())
                .fuente(request.fuente())
                .prioridad(request.prioridad() != null ? request.prioridad() : 100)
                .activo(true)
                .build();

        TecnicaVenta saved = tecnicaVentaRepository.save(tecnica);
        log.info("Técnica de venta creada: {} - {}", saved.getId(), saved.getNombre());
        return TecnicaVentaResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<TecnicaVentaResponse> crearMasivo(List<TecnicaVentaRequest> requests) {
        List<TecnicaVenta> tecnicas = requests.stream()
                .map(req -> TecnicaVenta.builder()
                        .categoria(req.categoria().toUpperCase())
                        .nombre(req.nombre())
                        .descripcion(req.descripcion())
                        .ejemplo(req.ejemplo())
                        .fuente(req.fuente())
                        .prioridad(req.prioridad() != null ? req.prioridad() : 100)
                        .activo(true)
                        .build())
                .collect(Collectors.toList());

        List<TecnicaVenta> saved = tecnicaVentaRepository.saveAll(tecnicas);
        log.info("Creadas {} técnicas de venta masivamente", saved.size());

        return saved.stream()
                .map(TecnicaVentaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TecnicaVentaResponse actualizar(Long id, TecnicaVentaRequest request) {
        TecnicaVenta tecnica = tecnicaVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TecnicaVenta", "id", id));

        tecnica.setCategoria(request.categoria().toUpperCase());
        tecnica.setNombre(request.nombre());
        tecnica.setDescripcion(request.descripcion());
        tecnica.setEjemplo(request.ejemplo());
        tecnica.setFuente(request.fuente());
        if (request.prioridad() != null) {
            tecnica.setPrioridad(request.prioridad());
        }

        TecnicaVenta saved = tecnicaVentaRepository.save(tecnica);
        log.info("Técnica de venta actualizada: {}", saved.getId());
        return TecnicaVentaResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        TecnicaVenta tecnica = tecnicaVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TecnicaVenta", "id", id));
        tecnica.setActivo(true);
        tecnicaVentaRepository.save(tecnica);
        log.info("Técnica de venta activada: {}", id);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        TecnicaVenta tecnica = tecnicaVentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TecnicaVenta", "id", id));
        tecnica.setActivo(false);
        tecnicaVentaRepository.save(tecnica);
        log.info("Técnica de venta desactivada: {}", id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!tecnicaVentaRepository.existsById(id)) {
            throw new ResourceNotFoundException("TecnicaVenta", "id", id);
        }
        tecnicaVentaRepository.deleteById(id);
        log.info("Técnica de venta eliminada: {}", id);
    }
}
