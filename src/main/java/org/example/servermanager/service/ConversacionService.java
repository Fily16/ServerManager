package org.example.servermanager.service;

import org.example.servermanager.entity.Conversacion;
import org.example.servermanager.enums.EstadoConversacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConversacionService {

    Conversacion crear(Long empresaId, String telefonoCliente, String nombreCliente);

    Conversacion obtenerOCrearActiva(Long empresaId, String telefonoCliente, String nombreCliente);

    Optional<Conversacion> obtenerPorId(Long empresaId, Long conversacionId);

    Optional<Conversacion> obtenerPorIdConMensajes(Long conversacionId);

    Optional<Conversacion> obtenerActiva(Long empresaId, String telefonoCliente);

    List<Conversacion> listarPorEmpresa(Long empresaId);

    Page<Conversacion> listarPorEmpresaPaginado(Long empresaId, Pageable pageable);

    List<Conversacion> listarPorEstado(Long empresaId, EstadoConversacion estado);

    List<Conversacion> listarActivas(Long empresaId);

    List<Conversacion> listarActivasRecientes(Long empresaId, int limite);

    List<Conversacion> obtenerInactivas(Long empresaId, LocalDateTime limite);

    void actualizarContextoAi(Long conversacionId, String contexto);

    void cambiarEstado(Long conversacionId, EstadoConversacion estado);

    void cerrar(Long conversacionId);

    void cerrarInactivas(Long empresaId, LocalDateTime limite);

    long contarPorEstado(Long empresaId, EstadoConversacion estado);

    long contarNuevasDesde(Long empresaId, LocalDateTime desde);

    Map<String, Object> obtenerEstadisticas(Long empresaId);
}
