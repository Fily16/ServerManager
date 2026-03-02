package org.example.servermanager.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.entity.ConfiguracionSaas;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.ConfiguracionSaasRepository;
import org.example.servermanager.service.ConfiguracionSaasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionSaasServiceImpl implements ConfiguracionSaasService {

    private final ConfiguracionSaasRepository configuracionSaasRepository;

    private static final String DEFAULT_MODELO = "gpt-4o-mini";
    private static final int DEFAULT_MAX_TOKENS = 500;

    @Override
    @Transactional
    public ConfiguracionSaas guardar(String clave, String valor, String descripcion) {
        ConfiguracionSaas config = configuracionSaasRepository.findByClave(clave)
                .map(existing -> {
                    existing.setValor(valor);
                    if (descripcion != null) {
                        existing.setDescripcion(descripcion);
                    }
                    return existing;
                })
                .orElse(ConfiguracionSaas.builder()
                        .clave(clave)
                        .valor(valor)
                        .descripcion(descripcion)
                        .build());

        return configuracionSaasRepository.save(config);
    }

    @Override
    public Optional<ConfiguracionSaas> obtenerPorClave(String clave) {
        return configuracionSaasRepository.findByClave(clave);
    }

    @Override
    public String obtenerValor(String clave) {
        return configuracionSaasRepository.findByClave(clave)
                .map(ConfiguracionSaas::getValor)
                .orElseThrow(() -> new ResourceNotFoundException("ConfiguracionSaas", "clave", clave));
    }

    @Override
    public String obtenerValorODefault(String clave, String defaultValue) {
        return configuracionSaasRepository.findByClave(clave)
                .map(ConfiguracionSaas::getValor)
                .orElse(defaultValue);
    }

    @Override
    public List<ConfiguracionSaas> listarTodas() {
        return configuracionSaasRepository.findAll();
    }

    @Override
    @Transactional
    public void eliminar(String clave) {
        configuracionSaasRepository.findByClave(clave)
                .ifPresent(configuracionSaasRepository::delete);
    }

    @Override
    public String obtenerOpenAiApiKey() {
        return obtenerValor(ConfiguracionSaas.OPENAI_API_KEY);
    }

    @Override
    public String obtenerOpenAiModelo() {
        return obtenerValorODefault(ConfiguracionSaas.OPENAI_MODELO_DEFAULT, DEFAULT_MODELO);
    }

    @Override
    public Integer obtenerOpenAiMaxTokens() {
        String valor = obtenerValorODefault(ConfiguracionSaas.OPENAI_MAX_TOKENS, String.valueOf(DEFAULT_MAX_TOKENS));
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_TOKENS;
        }
    }

    @Override
    public String obtenerElevenLabsApiKey() {
        return obtenerValorODefault(ConfiguracionSaas.ELEVENLABS_API_KEY, "");
    }

    @Override
    public String obtenerElevenLabsVoiceId() {
        return obtenerValorODefault(ConfiguracionSaas.ELEVENLABS_VOICE_ID, "");
    }
}
