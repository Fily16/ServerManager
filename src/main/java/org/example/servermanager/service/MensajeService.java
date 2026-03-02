package org.example.servermanager.service;

import org.example.servermanager.entity.Mensaje;
import org.example.servermanager.enums.TipoRemitente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MensajeService {

    Mensaje crear(Long conversacionId, Mensaje mensaje);

    Mensaje crearMensajeCliente(Long conversacionId, String contenido, String whatsappMessageId);

    Mensaje crearMensajeBot(Long conversacionId, String contenido, Integer tokensUsados);

    Optional<Mensaje> obtenerPorId(Long mensajeId);

    Optional<Mensaje> obtenerPorWhatsappMessageId(String whatsappMessageId);

    List<Mensaje> listarPorConversacion(Long conversacionId);

    Page<Mensaje> listarPorConversacionPaginado(Long conversacionId, Pageable pageable);

    List<Mensaje> obtenerUltimos(Long conversacionId, int cantidad);

    List<Mensaje> obtenerDesde(Long conversacionId, LocalDateTime desde);

    List<Mensaje> obtenerPorTipo(Long conversacionId, TipoRemitente tipo);

    int marcarComoLeidos(Long conversacionId);

    long contarPorConversacion(Long conversacionId);

    long contarNoLeidos(Long conversacionId);

    long sumarTokensPorConversacion(Long conversacionId);

    long sumarTokensPorEmpresaDesde(Long empresaId, LocalDateTime desde);

    boolean existePorWhatsappMessageId(String whatsappMessageId);
}
