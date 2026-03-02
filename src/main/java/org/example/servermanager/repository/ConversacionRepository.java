package org.example.servermanager.repository;

import org.example.servermanager.entity.Conversacion;
import org.example.servermanager.enums.EstadoConversacion;
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
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    List<Conversacion> findByEmpresaId(Long empresaId);

    Page<Conversacion> findByEmpresaId(Long empresaId, Pageable pageable);

    List<Conversacion> findByEmpresaIdAndEstado(Long empresaId, EstadoConversacion estado);

    Optional<Conversacion> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Conversacion> findByEmpresaIdAndTelefonoClienteAndEstado(Long empresaId, String telefono, EstadoConversacion estado);

    @Query("SELECT c FROM Conversacion c WHERE c.empresa.id = :empresaId AND c.telefonoCliente = :telefono " +
           "AND c.estado = 'ACTIVA' ORDER BY c.fechaInicio DESC")
    Optional<Conversacion> findConversacionActiva(@Param("empresaId") Long empresaId, @Param("telefono") String telefono);

    @Query("SELECT c FROM Conversacion c LEFT JOIN FETCH c.mensajes WHERE c.id = :id")
    Optional<Conversacion> findByIdWithMensajes(@Param("id") Long id);

    @Query("SELECT c FROM Conversacion c WHERE c.empresa.id = :empresaId AND c.estado = 'ACTIVA' " +
           "ORDER BY c.fechaUltimoMensaje DESC")
    List<Conversacion> findConversacionesActivas(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM Conversacion c WHERE c.empresa.id = :empresaId AND c.estado = 'ACTIVA' " +
           "ORDER BY c.fechaUltimoMensaje DESC")
    List<Conversacion> findConversacionesActivasRecientes(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM Conversacion c WHERE c.empresa.id = :empresaId AND c.estado = 'ACTIVA' " +
           "AND c.fechaUltimoMensaje < :limite")
    List<Conversacion> findConversacionesInactivas(@Param("empresaId") Long empresaId, @Param("limite") LocalDateTime limite);

    @Query("SELECT c FROM Conversacion c WHERE c.empresa.id = :empresaId AND c.fechaInicio >= :desde")
    List<Conversacion> findConversacionesDesde(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);

    @Modifying
    @Query("UPDATE Conversacion c SET c.estado = 'CERRADA', c.fechaCierre = :fecha " +
           "WHERE c.estado = 'ACTIVA' AND c.fechaUltimoMensaje < :limite")
    int cerrarConversacionesInactivas(@Param("fecha") LocalDateTime fecha, @Param("limite") LocalDateTime limite);

    long countByEmpresaId(Long empresaId);

    long countByEmpresaIdAndEstado(Long empresaId, EstadoConversacion estado);

    @Query("SELECT COUNT(c) FROM Conversacion c WHERE c.empresa.id = :empresaId AND c.fechaInicio >= :desde")
    long countConversacionesDesde(@Param("empresaId") Long empresaId, @Param("desde") LocalDateTime desde);
}
