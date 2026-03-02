package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.Cliente;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.exception.DuplicateResourceException;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.ClienteRepository;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public Cliente crear(Long empresaId, Cliente cliente) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        if (clienteRepository.existsByEmpresaIdAndTelefono(empresaId, cliente.getTelefono())) {
            throw new DuplicateResourceException("Cliente", "telefono", cliente.getTelefono());
        }

        cliente.setEmpresa(empresa);
        cliente.setUltimaInteraccion(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente crearOActualizar(Long empresaId, String telefono, String nombre) {
        return clienteRepository.findByEmpresaIdAndTelefono(empresaId, telefono)
                .map(cliente -> {
                    if (nombre != null && !nombre.isBlank()) {
                        cliente.setNombre(nombre);
                    }
                    cliente.setUltimaInteraccion(LocalDateTime.now());
                    return clienteRepository.save(cliente);
                })
                .orElseGet(() -> {
                    Cliente nuevoCliente = Cliente.builder()
                            .telefono(telefono)
                            .nombre(nombre)
                            .empresa(empresaRepository.getReferenceById(empresaId))
                            .ultimaInteraccion(LocalDateTime.now())
                            .build();
                    return clienteRepository.save(nuevoCliente);
                });
    }

    @Override
    @Transactional
    public Cliente actualizar(Long empresaId, Long clienteId, Cliente cliente) {
        Cliente existente = clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        existente.setNombre(cliente.getNombre());
        existente.setEmail(cliente.getEmail());
        existente.setDistrito(cliente.getDistrito());
        existente.setDireccion(cliente.getDireccion());

        return clienteRepository.save(existente);
    }

    @Override
    public Optional<Cliente> obtenerPorId(Long empresaId, Long clienteId) {
        return clienteRepository.findByIdAndEmpresaId(clienteId, empresaId);
    }

    @Override
    public Optional<Cliente> obtenerPorTelefono(Long empresaId, String telefono) {
        return clienteRepository.findByEmpresaIdAndTelefono(empresaId, telefono);
    }

    @Override
    public List<Cliente> listarPorEmpresa(Long empresaId) {
        return clienteRepository.findByEmpresaId(empresaId);
    }

    @Override
    public Page<Cliente> listarPorEmpresaPaginado(Long empresaId, Pageable pageable) {
        return clienteRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    public List<Cliente> buscarPorTexto(Long empresaId, String busqueda) {
        return clienteRepository.buscarPorTexto(empresaId, busqueda);
    }

    @Override
    public List<Cliente> obtenerRecientes(Long empresaId, LocalDateTime desde) {
        return clienteRepository.findClientesActivos(empresaId, desde);
    }

    @Override
    public List<Cliente> obtenerTopClientes(Long empresaId, int limite) {
        return clienteRepository.findTopClientesPorPedidos(empresaId, PageRequest.of(0, limite));
    }

    @Override
    @Transactional
    public void actualizarInteraccion(Long clienteId) {
        clienteRepository.findById(clienteId).ifPresent(cliente -> {
            cliente.setUltimaInteraccion(LocalDateTime.now());
            clienteRepository.save(cliente);
        });
    }

    @Override
    @Transactional
    public void incrementarConversaciones(Long clienteId) {
        clienteRepository.findById(clienteId).ifPresent(cliente -> {
            cliente.setTotalConversaciones(cliente.getTotalConversaciones() + 1);
            clienteRepository.save(cliente);
        });
    }

    @Override
    @Transactional
    public void incrementarPedidos(Long clienteId) {
        clienteRepository.findById(clienteId).ifPresent(cliente -> {
            cliente.setTotalPedidos(cliente.getTotalPedidos() + 1);
            clienteRepository.save(cliente);
        });
    }

    @Override
    @Transactional
    public void eliminar(Long empresaId, Long clienteId) {
        clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
                .ifPresent(clienteRepository::delete);
    }

    @Override
    public long contarPorEmpresa(Long empresaId) {
        return clienteRepository.countByEmpresaId(empresaId);
    }

    @Override
    public long contarNuevosDesde(Long empresaId, LocalDateTime desde) {
        return clienteRepository.countClientesActivos(empresaId, desde);
    }
}
