package org.example.servermanager.repository;

import org.example.servermanager.entity.ConfiguracionSaas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionSaasRepository extends JpaRepository<ConfiguracionSaas, Long> {

    Optional<ConfiguracionSaas> findByClave(String clave);

    boolean existsByClave(String clave);
}
