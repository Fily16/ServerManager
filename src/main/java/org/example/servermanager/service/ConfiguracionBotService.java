package org.example.servermanager.service;

import org.example.servermanager.entity.ConfiguracionBot;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionBotService {

    ConfiguracionBot crear(Long empresaId, ConfiguracionBot configuracion);

    ConfiguracionBot actualizar(Long empresaId, ConfiguracionBot configuracion);

    Optional<ConfiguracionBot> obtenerPorEmpresaId(Long empresaId);

    Optional<ConfiguracionBot> obtenerPorNumeroWhatsapp(String numero);

    List<ConfiguracionBot> listarActivas();

    List<ConfiguracionBot> listarConVerificacionPagos();

    void activar(Long empresaId);

    void desactivar(Long empresaId);

    boolean existePorNumeroWhatsapp(String numero);

    void toggleVerificacionPagos(Long empresaId, boolean activo);
}
