package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.enums.Plan;
import org.example.servermanager.exception.DuplicateResourceException;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.service.EmpresaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public Empresa crear(Empresa empresa) {
        if (empresa.getRuc() != null && empresaRepository.existsByRuc(empresa.getRuc())) {
            throw new DuplicateResourceException("Empresa", "ruc", empresa.getRuc());
        }
        return empresaRepository.save(empresa);
    }

    @Override
    @Transactional
    public Empresa actualizar(Long id, Empresa empresa) {
        Empresa existente = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", id));

        existente.setNombre(empresa.getNombre());
        existente.setRuc(empresa.getRuc());
        existente.setEmail(empresa.getEmail());
        existente.setTelefono(empresa.getTelefono());
        existente.setLogoUrl(empresa.getLogoUrl());
        existente.setDireccionFiscal(empresa.getDireccionFiscal());
        existente.setPlan(empresa.getPlan());

        return empresaRepository.save(existente);
    }

    @Override
    public Optional<Empresa> obtenerPorId(Long id) {
        return empresaRepository.findById(id);
    }

    @Override
    public Optional<Empresa> obtenerPorIdConConfiguracion(Long id) {
        return empresaRepository.findByIdWithConfiguracion(id);
    }

    @Override
    public Optional<Empresa> obtenerPorRuc(String ruc) {
        return empresaRepository.findByRuc(ruc);
    }

    @Override
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    @Override
    public List<Empresa> listarActivas() {
        return empresaRepository.findByActivoTrue();
    }

    @Override
    public List<Empresa> listarPorPlan(Plan plan) {
        return empresaRepository.findByPlan(plan);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        empresaRepository.findById(id).ifPresent(empresa -> {
            empresa.setActivo(true);
            empresaRepository.save(empresa);
        });
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        empresaRepository.findById(id).ifPresent(empresa -> {
            empresa.setActivo(false);
            empresaRepository.save(empresa);
        });
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        empresaRepository.deleteById(id);
    }

    @Override
    public boolean existePorRuc(String ruc) {
        return empresaRepository.existsByRuc(ruc);
    }

    @Override
    public long contarActivas() {
        return empresaRepository.countActivas();
    }
}
