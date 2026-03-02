package org.example.servermanager.repository;

import org.example.servermanager.entity.ConfiguracionBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionBotRepository extends JpaRepository<ConfiguracionBot, Long> {

    Optional<ConfiguracionBot> findByEmpresaId(Long empresaId);

    Optional<ConfiguracionBot> findByNumeroWhatsapp(String numeroWhatsapp);

    Optional<ConfiguracionBot> findByEvolutionInstancia(String instancia);

    List<ConfiguracionBot> findByActivoTrue();

    List<ConfiguracionBot> findByVerificacionPagosActivoTrue();

    @Query("SELECT c FROM ConfiguracionBot c WHERE c.activo = true AND c.empresa.activo = true")
    List<ConfiguracionBot> findConfiguracionesActivas();

    boolean existsByNumeroWhatsapp(String numeroWhatsapp);

    boolean existsByEvolutionInstancia(String instancia);

    @Query("SELECT c FROM ConfiguracionBot c JOIN FETCH c.empresa WHERE c.numeroWhatsapp = :numero")
    Optional<ConfiguracionBot> findByNumeroWhatsappWithEmpresa(@Param("numero") String numero);
}
