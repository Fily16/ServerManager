package org.example.servermanager.repository;

import org.example.servermanager.entity.MensajeCampana;
import org.example.servermanager.enums.EstadoMensajeCampana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeCampanaRepository extends JpaRepository<MensajeCampana, Long> {

    @Modifying
    @Query("DELETE FROM MensajeCampana mc WHERE mc.fechaCreacion < :antes")
    int deleteByFechaCreacionBefore(@Param("antes") LocalDateTime antes);

    List<MensajeCampana> findByCampanaIdOrderByOrdenAsc(Long campanaId);

    List<MensajeCampana> findByCampanaIdAndEstado(Long campanaId, EstadoMensajeCampana estado);

    /** Obtiene el siguiente mensaje pendiente de una campana (el de menor orden) */
    @Query("SELECT m FROM MensajeCampana m WHERE m.campana.id = :campanaId " +
           "AND m.estado = 'PENDIENTE' ORDER BY m.orden ASC LIMIT 1")
    Optional<MensajeCampana> findSiguientePendiente(@Param("campanaId") Long campanaId);

    long countByCampanaIdAndEstado(Long campanaId, EstadoMensajeCampana estado);

    /** Verifica si ya existe un contacto en la campana (evitar duplicados) */
    boolean existsByCampanaIdAndTelefono(Long campanaId, String telefono);

    /** Busca si un telefono pertenece a alguna campana activa (EN_PROGRESO o PAUSADA) de una empresa */
    @Query("SELECT mc FROM MensajeCampana mc JOIN mc.campana c " +
           "WHERE c.empresa.id = :empresaId AND mc.telefono = :telefono " +
           "AND c.estado IN ('EN_PROGRESO', 'PAUSADA', 'COMPLETADA') " +
           "ORDER BY mc.fechaCreacion DESC LIMIT 1")
    Optional<MensajeCampana> findContactoCampanaActiva(
            @Param("empresaId") Long empresaId,
            @Param("telefono") String telefono);

    /** Obtiene todos los telefonos ya contactados (ENVIADO) en campanas previas para el mismo grupo */
    @Query("SELECT DISTINCT mc.telefono FROM MensajeCampana mc JOIN mc.campana c " +
           "WHERE c.groupJid = :groupJid AND mc.estado = 'ENVIADO'")
    List<String> findTelefonosYaEnviadosPorGrupo(@Param("groupJid") String groupJid);
}
