package org.example.servermanager.repository;

import org.example.servermanager.entity.Mensaje;
import org.example.servermanager.enums.TipoRemitente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByConversacionId(Long conversacionId);

    Page<Mensaje> findByConversacionId(Long conversacionId, Pageable pageable);

    List<Mensaje> findByConversacionIdOrderByFechaEnvioAsc(Long conversacionId);

    List<Mensaje> findByConversacionIdAndTipoRemitente(Long conversacionId, TipoRemitente tipoRemitente);

    Optional<Mensaje> findByWhatsappMessageId(String whatsappMessageId);

    @Query("SELECT m FROM Mensaje m WHERE m.conversacion.id = :conversacionId ORDER BY m.fechaEnvio DESC")
    List<Mensaje> findUltimosMensajes(@Param("conversacionId") Long conversacionId, Pageable pageable);

    @Query("SELECT m FROM Mensaje m WHERE m.conversacion.empresa.id = :empresaId AND m.fechaEnvio >= :desde")
    List<Mensaje> findMensajesDesde(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    @Query("SELECT m FROM Mensaje m WHERE m.conversacion.id = :conversacionId AND m.fechaEnvio >= :desde ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findMensajesPorConversacionDesde(@Param("conversacionId") Long conversacionId, @Param("desde") LocalDateTime desde);

    @Query("SELECT SUM(m.tokensUsados) FROM Mensaje m WHERE m.conversacion.empresa.id = :empresaId " +
           "AND m.fechaEnvio >= :desde AND m.fechaEnvio < :hasta")
    Long sumTokensUsados(@Param("empresaId") Long empresaId, 
                         @Param("desde") LocalDateTime desde, 
                         @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(m.tokensUsados), 0) FROM Mensaje m WHERE m.conversacion.id = :conversacionId")
    long sumTokensByConversacionId(@Param("conversacionId") Long conversacionId);

    @Query("SELECT COALESCE(SUM(m.tokensUsados), 0) FROM Mensaje m WHERE m.conversacion.empresa.id = :empresaId " +
           "AND m.fechaEnvio >= :desde")
    long sumTokensByEmpresaIdDesde(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    @Modifying
    @Query("UPDATE Mensaje m SET m.leido = true WHERE m.conversacion.id = :conversacionId AND m.leido = false")
    int marcarComoLeidos(@Param("conversacionId") Long conversacionId);

    long countByConversacionId(Long conversacionId);

    long countByConversacionIdAndLeidoFalse(Long conversacionId);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.conversacion.empresa.id = :empresaId AND m.fechaEnvio >= :desde")
    long countMensajesDesde(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    boolean existsByWhatsappMessageId(String whatsappMessageId);

    @Modifying
    @Query("DELETE FROM Mensaje m WHERE m.fechaEnvio < :antes")
    int deleteByFechaEnvioBefore(@Param("antes") LocalDateTime antes);

    /**
     * Verifica si el bot ha respondido en esta conversacion recientemente (para mantener contexto)
     */
    @Query("SELECT COUNT(m) > 0 FROM Mensaje m WHERE m.conversacion.id = :conversacionId " +
           "AND m.tipoRemitente = 'BOT' AND m.fechaEnvio >= :desde")
    boolean existeRespuestaBotReciente(@Param("conversacionId") Long conversacionId,
                                       @Param("desde") LocalDateTime desde);
}
