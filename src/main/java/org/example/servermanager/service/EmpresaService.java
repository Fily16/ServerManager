package org.example.servermanager.service;

import org.example.servermanager.entity.Empresa;
import org.example.servermanager.enums.Plan;

import java.util.List;
import java.util.Optional;

public interface EmpresaService {

    Empresa crear(Empresa empresa);

    Empresa actualizar(Long id, Empresa empresa);

    Optional<Empresa> obtenerPorId(Long id);

    Optional<Empresa> obtenerPorIdConConfiguracion(Long id);

    Optional<Empresa> obtenerPorRuc(String ruc);

    List<Empresa> listarTodas();

    List<Empresa> listarActivas();

    List<Empresa> listarPorPlan(Plan plan);

    void activar(Long id);

    void desactivar(Long id);

    void eliminar(Long id);

    boolean existePorRuc(String ruc);

    long contarActivas();
}
