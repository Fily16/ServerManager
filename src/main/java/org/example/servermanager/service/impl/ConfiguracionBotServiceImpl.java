package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.ConfiguracionBot;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.exception.DuplicateResourceException;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.ConfiguracionBotRepository;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.service.ConfiguracionBotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionBotServiceImpl implements ConfiguracionBotService {

    private final ConfiguracionBotRepository configuracionBotRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public ConfiguracionBot crear(Long empresaId, ConfiguracionBot configuracion) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        if (configuracionBotRepository.findByEmpresaId(empresaId).isPresent()) {
            throw new DuplicateResourceException("ConfiguracionBot", "empresaId", empresaId);
        }

        configuracion.setEmpresa(empresa);
        return configuracionBotRepository.save(configuracion);
    }

    @Override
    @Transactional
    public ConfiguracionBot actualizar(Long empresaId, ConfiguracionBot configuracion) {
        ConfiguracionBot existente = configuracionBotRepository.findByEmpresaId(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("ConfiguracionBot", "empresaId", empresaId));

        existente.setEvolutionApiUrl(configuracion.getEvolutionApiUrl());
        existente.setEvolutionApiKey(configuracion.getEvolutionApiKey());
        existente.setEvolutionInstancia(configuracion.getEvolutionInstancia());
        existente.setNumeroWhatsapp(configuracion.getNumeroWhatsapp());
        existente.setNombreBot(configuracion.getNombreBot());
        existente.setMensajeBienvenida(configuracion.getMensajeBienvenida());
        existente.setPromptSistema(configuracion.getPromptSistema());
        existente.setTonoConversacion(configuracion.getTonoConversacion());
        existente.setModeloAi(configuracion.getModeloAi());
        existente.setVerificacionPagosActivo(configuracion.getVerificacionPagosActivo());
        existente.setEmailNotificacionesPago(configuracion.getEmailNotificacionesPago());
        existente.setEmailPasswordApp(configuracion.getEmailPasswordApp());
        existente.setLinkGrupoConsolidado(configuracion.getLinkGrupoConsolidado());
        existente.setLinkCatalogo(configuracion.getLinkCatalogo());
        existente.setTiempoEntrega(configuracion.getTiempoEntrega());
        existente.setLinkTiktok(configuracion.getLinkTiktok());
        existente.setPromptCampana(configuracion.getPromptCampana());
        existente.setHorarioInicio(configuracion.getHorarioInicio());
        existente.setHorarioFin(configuracion.getHorarioFin());
        existente.setMensajeFueraHorario(configuracion.getMensajeFueraHorario());
        existente.setAutoRespuesta(configuracion.getAutoRespuesta());

        return configuracionBotRepository.save(existente);
    }

    @Override
    public Optional<ConfiguracionBot> obtenerPorEmpresaId(Long empresaId) {
        return configuracionBotRepository.findByEmpresaId(empresaId);
    }

    @Override
    public Optional<ConfiguracionBot> obtenerPorNumeroWhatsapp(String numero) {
        return configuracionBotRepository.findByNumeroWhatsappWithEmpresa(numero);
    }

    @Override
    public List<ConfiguracionBot> listarActivas() {
        return configuracionBotRepository.findConfiguracionesActivas();
    }

    @Override
    public List<ConfiguracionBot> listarConVerificacionPagos() {
        return configuracionBotRepository.findByVerificacionPagosActivoTrue();
    }

    @Override
    @Transactional
    public void activar(Long empresaId) {
        configuracionBotRepository.findByEmpresaId(empresaId).ifPresent(config -> {
            config.setActivo(true);
            configuracionBotRepository.save(config);
        });
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId) {
        configuracionBotRepository.findByEmpresaId(empresaId).ifPresent(config -> {
            config.setActivo(false);
            configuracionBotRepository.save(config);
        });
    }

    @Override
    public boolean existePorNumeroWhatsapp(String numero) {
        return configuracionBotRepository.existsByNumeroWhatsapp(numero);
    }

    @Override
    @Transactional
    public void toggleVerificacionPagos(Long empresaId, boolean activo) {
        configuracionBotRepository.findByEmpresaId(empresaId).ifPresent(config -> {
            config.setVerificacionPagosActivo(activo);
            configuracionBotRepository.save(config);
        });
    }
}
