package org.example.servermanager.service;

import org.example.servermanager.entity.Sede;

import java.util.List;
import java.util.Optional;

public interface SedeService {

    Sede crear(Long empresaId, Sede sede);

    Sede actualizar(Long empresaId, Long sedeId, Sede sede);

    Optional<Sede> obtenerPorId(Long empresaId, Long sedeId);

    List<Sede> listarPorEmpresa(Long empresaId);

    List<Sede> listarActivasPorEmpresa(Long empresaId);

    Optional<Sede> obtenerSedePrincipal(Long empresaId);

    void establecerComoPrincipal(Long empresaId, Long sedeId);

    void activar(Long empresaId, Long sedeId);

    void desactivar(Long empresaId, Long sedeId);

    void eliminar(Long empresaId, Long sedeId);

    long contarPorEmpresa(Long empresaId);
}
