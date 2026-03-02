package org.example.servermanager.service;

import org.example.servermanager.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClienteService {

    Cliente crear(Long empresaId, Cliente cliente);

    Cliente crearOActualizar(Long empresaId, String telefono, String nombre);

    Cliente actualizar(Long empresaId, Long clienteId, Cliente cliente);

    Optional<Cliente> obtenerPorId(Long empresaId, Long clienteId);

    Optional<Cliente> obtenerPorTelefono(Long empresaId, String telefono);

    List<Cliente> listarPorEmpresa(Long empresaId);

    Page<Cliente> listarPorEmpresaPaginado(Long empresaId, Pageable pageable);

    List<Cliente> buscarPorTexto(Long empresaId, String busqueda);

    List<Cliente> obtenerRecientes(Long empresaId, LocalDateTime desde);

    List<Cliente> obtenerTopClientes(Long empresaId, int limite);

    void actualizarInteraccion(Long clienteId);

    void incrementarConversaciones(Long clienteId);

    void incrementarPedidos(Long clienteId);

    void eliminar(Long empresaId, Long clienteId);

    long contarPorEmpresa(Long empresaId);

    long contarNuevosDesde(Long empresaId, LocalDateTime desde);
}
