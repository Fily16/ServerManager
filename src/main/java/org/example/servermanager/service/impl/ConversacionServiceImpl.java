package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.Cliente;
import org.example.servermanager.entity.Conversacion;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.enums.EstadoConversacion;
import org.example.servermanager.repository.ClienteRepository;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.ConversacionRepository;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.service.ClienteService;
import org.example.servermanager.service.ConversacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversacionServiceImpl implements ConversacionService {

    private final ConversacionRepository conversacionRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteService clienteService;

    @Override
    @Transactional
    public Conversacion crear(Long empresaId, String telefonoCliente, String nombreCliente) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        Cliente cliente = clienteService.crearOActualizar(empresaId, telefonoCliente, nombreCliente);
        clienteService.incrementarConversaciones(cliente.getId());

        Conversacion conversacion = Conversacion.builder()
                .empresa(empresa)
                .cliente(cliente)
                .telefonoCliente(telefonoCliente)
                .nombreCliente(nombreCliente)
                .estado(EstadoConversacion.ACTIVA)
                .fechaUltimoMensaje(LocalDateTime.now())
                .build();

        return conversacionRepository.save(conversacion);
    }

    @Override
    @Transactional
    public Conversacion obtenerOCrearActiva(Long empresaId, String telefonoCliente, String nombreCliente) {
        return conversacionRepository.findConversacionActiva(empresaId, telefonoCliente)
                .orElseGet(() -> crear(empresaId, telefonoCliente, nombreCliente));
    }

    @Override
    public Optional<Conversacion> obtenerPorId(Long empresaId, Long conversacionId) {
        return conversacionRepository.findByIdAndEmpresaId(conversacionId, empresaId);
    }

    @Override
    public Optional<Conversacion> obtenerPorIdConMensajes(Long conversacionId) {
        return conversacionRepository.findByIdWithMensajes(conversacionId);
    }

    @Override
    public Optional<Conversacion> obtenerActiva(Long empresaId, String telefonoCliente) {
        return conversacionRepository.findConversacionActiva(empresaId, telefonoCliente);
    }

    @Override
    public List<Conversacion> listarPorEmpresa(Long empresaId) {
        return conversacionRepository.findByEmpresaId(empresaId);
    }

    @Override
    public Page<Conversacion> listarPorEmpresaPaginado(Long empresaId, Pageable pageable) {
        return conversacionRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    public List<Conversacion> listarPorEstado(Long empresaId, EstadoConversacion estado) {
        return conversacionRepository.findByEmpresaIdAndEstado(empresaId, estado);
    }

    @Override
    public List<Conversacion> listarActivas(Long empresaId) {
        return conversacionRepository.findConversacionesActivas(empresaId);
    }

    @Override
    public List<Conversacion> listarActivasRecientes(Long empresaId, int limite) {
        return conversacionRepository.findConversacionesActivasRecientes(empresaId, PageRequest.of(0, limite));
    }

    @Override
    public List<Conversacion> obtenerInactivas(Long empresaId, LocalDateTime limite) {
        return conversacionRepository.findConversacionesInactivas(empresaId, limite);
    }

    @Override
    @Transactional
    public void actualizarContextoAi(Long conversacionId, String contexto) {
        conversacionRepository.findById(conversacionId).ifPresent(conv -> {
            conv.setContextoAi(contexto);
            conversacionRepository.save(conv);
        });
    }

    @Override
    @Transactional
    public void cambiarEstado(Long conversacionId, EstadoConversacion estado) {
        conversacionRepository.findById(conversacionId).ifPresent(conv -> {
            conv.setEstado(estado);
            if (estado == EstadoConversacion.CERRADA) {
                conv.setFechaCierre(LocalDateTime.now());
            }
            conversacionRepository.save(conv);
        });
    }

    @Override
    @Transactional
    public void cerrar(Long conversacionId) {
        conversacionRepository.findById(conversacionId).ifPresent(conv -> {
            conv.cerrar();
            conversacionRepository.save(conv);
        });
    }

    @Override
    @Transactional
    public void cerrarInactivas(Long empresaId, LocalDateTime limite) {
        List<Conversacion> inactivas = conversacionRepository.findConversacionesInactivas(empresaId, limite);
        inactivas.forEach(conv -> {
            conv.cerrar();
            conversacionRepository.save(conv);
        });
    }

    @Override
    public long contarPorEstado(Long empresaId, EstadoConversacion estado) {
        return conversacionRepository.countByEmpresaIdAndEstado(empresaId, estado);
    }

    @Override
    public long contarNuevasDesde(Long empresaId, LocalDateTime desde) {
        return conversacionRepository.countConversacionesDesde(empresaId, desde);
    }

    @Override
    public Map<String, Object> obtenerEstadisticas(Long empresaId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", conversacionRepository.countByEmpresaId(empresaId));
        stats.put("activas", conversacionRepository.countByEmpresaIdAndEstado(empresaId, EstadoConversacion.ACTIVA));
        stats.put("cerradas", conversacionRepository.countByEmpresaIdAndEstado(empresaId, EstadoConversacion.CERRADA));
        stats.put("nuevasUltimas24h", conversacionRepository.countConversacionesDesde(empresaId, LocalDateTime.now().minusHours(24)));
        return stats;
    }
}
