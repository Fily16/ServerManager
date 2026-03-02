package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.entity.Sede;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.repository.SedeRepository;
import org.example.servermanager.service.SedeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SedeServiceImpl implements SedeService {

    private final SedeRepository sedeRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public Sede crear(Long empresaId, Sede sede) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        sede.setEmpresa(empresa);

        if (sedeRepository.countByEmpresaId(empresaId) == 0) {
            sede.setEsPrincipal(true);
        }

        return sedeRepository.save(sede);
    }

    @Override
    @Transactional
    public Sede actualizar(Long empresaId, Long sedeId, Sede sede) {
        Sede existente = sedeRepository.findByIdAndEmpresaId(sedeId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede", "id", sedeId));

        existente.setNombre(sede.getNombre());
        existente.setDireccion(sede.getDireccion());
        existente.setDistrito(sede.getDistrito());
        existente.setCiudad(sede.getCiudad());
        existente.setReferencia(sede.getReferencia());
        existente.setTelefono(sede.getTelefono());
        existente.setHorarioAtencion(sede.getHorarioAtencion());
        existente.setLatitud(sede.getLatitud());
        existente.setLongitud(sede.getLongitud());

        return sedeRepository.save(existente);
    }

    @Override
    public Optional<Sede> obtenerPorId(Long empresaId, Long sedeId) {
        return sedeRepository.findByIdAndEmpresaId(sedeId, empresaId);
    }

    @Override
    public List<Sede> listarPorEmpresa(Long empresaId) {
        return sedeRepository.findByEmpresaId(empresaId);
    }

    @Override
    public List<Sede> listarActivasPorEmpresa(Long empresaId) {
        return sedeRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    @Override
    public Optional<Sede> obtenerSedePrincipal(Long empresaId) {
        return sedeRepository.findByEmpresaIdAndEsPrincipalTrue(empresaId);
    }

    @Override
    @Transactional
    public void establecerComoPrincipal(Long empresaId, Long sedeId) {
        sedeRepository.findByEmpresaIdAndEsPrincipalTrue(empresaId)
                .ifPresent(sedePrincipal -> {
                    sedePrincipal.setEsPrincipal(false);
                    sedeRepository.save(sedePrincipal);
                });

        sedeRepository.findByIdAndEmpresaId(sedeId, empresaId)
                .ifPresent(sede -> {
                    sede.setEsPrincipal(true);
                    sedeRepository.save(sede);
                });
    }

    @Override
    @Transactional
    public void activar(Long empresaId, Long sedeId) {
        sedeRepository.findByIdAndEmpresaId(sedeId, empresaId).ifPresent(sede -> {
            sede.setActivo(true);
            sedeRepository.save(sede);
        });
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long sedeId) {
        sedeRepository.findByIdAndEmpresaId(sedeId, empresaId).ifPresent(sede -> {
            sede.setActivo(false);
            sedeRepository.save(sede);
        });
    }

    @Override
    @Transactional
    public void eliminar(Long empresaId, Long sedeId) {
        sedeRepository.findByIdAndEmpresaId(sedeId, empresaId)
                .ifPresent(sedeRepository::delete);
    }

    @Override
    public long contarPorEmpresa(Long empresaId) {
        return sedeRepository.countByEmpresaId(empresaId);
    }
}
