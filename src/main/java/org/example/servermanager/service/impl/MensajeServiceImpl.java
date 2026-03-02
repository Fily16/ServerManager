package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.Conversacion;
import org.example.servermanager.entity.Mensaje;
import org.example.servermanager.enums.TipoContenido;
import org.example.servermanager.enums.TipoRemitente;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.ConversacionRepository;
import org.example.servermanager.repository.MensajeRepository;
import org.example.servermanager.service.MensajeService;
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
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;

    @Override
    @Transactional
    public Mensaje crear(Long conversacionId, Mensaje mensaje) {
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación", "id", conversacionId));

        mensaje.setConversacion(conversacion);
        Mensaje mensajeGuardado = mensajeRepository.save(mensaje);

        conversacion.setTotalMensajes(conversacion.getTotalMensajes() + 1);
        conversacion.setFechaUltimoMensaje(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        return mensajeGuardado;
    }

    @Override
    @Transactional
    public Mensaje crearMensajeCliente(Long conversacionId, String contenido, String whatsappMessageId) {
        Mensaje mensaje = Mensaje.builder()
                .tipoRemitente(TipoRemitente.CLIENTE)
                .contenido(contenido)
                .tipoContenido(TipoContenido.TEXTO)
                .whatsappMessageId(whatsappMessageId)
                .leido(false)
                .build();

        return crear(conversacionId, mensaje);
    }

    @Override
    @Transactional
    public Mensaje crearMensajeBot(Long conversacionId, String contenido, Integer tokensUsados) {
        Mensaje mensaje = Mensaje.builder()
                .tipoRemitente(TipoRemitente.BOT)
                .contenido(contenido)
                .tipoContenido(TipoContenido.TEXTO)
                .tokensUsados(tokensUsados)
                .leido(true)
                .build();

        return crear(conversacionId, mensaje);
    }

    @Override
    public Optional<Mensaje> obtenerPorId(Long mensajeId) {
        return mensajeRepository.findById(mensajeId);
    }

    @Override
    public Optional<Mensaje> obtenerPorWhatsappMessageId(String whatsappMessageId) {
        return mensajeRepository.findByWhatsappMessageId(whatsappMessageId);
    }

    @Override
    public List<Mensaje> listarPorConversacion(Long conversacionId) {
        return mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(conversacionId);
    }

    @Override
    public Page<Mensaje> listarPorConversacionPaginado(Long conversacionId, Pageable pageable) {
        return mensajeRepository.findByConversacionId(conversacionId, pageable);
    }

    @Override
    public List<Mensaje> obtenerUltimos(Long conversacionId, int cantidad) {
        return mensajeRepository.findUltimosMensajes(conversacionId, PageRequest.of(0, cantidad));
    }

    @Override
    public List<Mensaje> obtenerDesde(Long conversacionId, LocalDateTime desde) {
        return mensajeRepository.findMensajesPorConversacionDesde(conversacionId, desde);
    }

    @Override
    public List<Mensaje> obtenerPorTipo(Long conversacionId, TipoRemitente tipo) {
        return mensajeRepository.findByConversacionIdAndTipoRemitente(conversacionId, tipo);
    }

    @Override
    @Transactional
    public int marcarComoLeidos(Long conversacionId) {
        return mensajeRepository.marcarComoLeidos(conversacionId);
    }

    @Override
    public long contarPorConversacion(Long conversacionId) {
        return mensajeRepository.countByConversacionId(conversacionId);
    }

    @Override
    public long contarNoLeidos(Long conversacionId) {
        return mensajeRepository.countByConversacionIdAndLeidoFalse(conversacionId);
    }

    @Override
    public long sumarTokensPorConversacion(Long conversacionId) {
        return mensajeRepository.sumTokensByConversacionId(conversacionId);
    }

    @Override
    public long sumarTokensPorEmpresaDesde(Long empresaId, LocalDateTime desde) {
        return mensajeRepository.sumTokensByEmpresaIdDesde(empresaId, desde);
    }

    @Override
    public boolean existePorWhatsappMessageId(String whatsappMessageId) {
        return mensajeRepository.existsByWhatsappMessageId(whatsappMessageId);
    }
}
