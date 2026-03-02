package org.example.servermanager.service;

import org.example.servermanager.entity.ConfiguracionSaas;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionSaasService {

    ConfiguracionSaas guardar(String clave, String valor, String descripcion);

    Optional<ConfiguracionSaas> obtenerPorClave(String clave);

    String obtenerValor(String clave);

    String obtenerValorODefault(String clave, String defaultValue);

    List<ConfiguracionSaas> listarTodas();

    void eliminar(String clave);

    // Métodos específicos para OpenAI
    String obtenerOpenAiApiKey();

    String obtenerOpenAiModelo();

    Integer obtenerOpenAiMaxTokens();

    // Métodos específicos para ElevenLabs
    String obtenerElevenLabsApiKey();

    String obtenerElevenLabsVoiceId();
}
