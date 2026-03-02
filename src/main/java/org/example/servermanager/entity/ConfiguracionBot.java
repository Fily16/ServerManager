package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.TonoConversacion;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "configuracion_bot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionBot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== EVOLUTION API (WhatsApp) ====================

    @Column(name = "evolution_api_url", length = 500)
    private String evolutionApiUrl;

    @Column(name = "evolution_api_key")
    private String evolutionApiKey;

    @Column(name = "evolution_instancia", length = 100)
    private String evolutionInstancia;

    @Column(name = "numero_whatsapp", length = 20)
    private String numeroWhatsapp;

    // ==================== PERSONALIZACIÓN DEL BOT ====================

    @Column(name = "nombre_bot", length = 100)
    @Builder.Default
    private String nombreBot = "Asistente";

    @Column(name = "mensaje_bienvenida", columnDefinition = "TEXT")
    private String mensajeBienvenida;

    @Column(name = "prompt_sistema", columnDefinition = "TEXT")
    private String promptSistema;

    @Enumerated(EnumType.STRING)
    @Column(name = "tono_conversacion", length = 50)
    @Builder.Default
    private TonoConversacion tonoConversacion = TonoConversacion.AMIGABLE;

    @Column(name = "modelo_ai", length = 50)
    @Builder.Default
    private String modeloAi = "gpt-4o-mini";

    // ==================== INFO COMERCIAL ====================

    @Column(name = "link_grupo_consolidado", length = 500)
    private String linkGrupoConsolidado;

    @Column(name = "link_catalogo", length = 500)
    private String linkCatalogo;

    @Column(name = "tiempo_entrega", length = 200)
    @Builder.Default
    private String tiempoEntrega = "1 a 2 semanas";

    @Column(name = "link_tiktok", length = 500)
    private String linkTiktok;

    @Column(name = "prompt_campana", columnDefinition = "TEXT")
    private String promptCampana;

    // ==================== VERIFICACIÓN DE PAGOS ====================

    @Column(name = "verificacion_pagos_activo")
    @Builder.Default
    private Boolean verificacionPagosActivo = false;

    @Column(name = "email_notificaciones_pago")
    private String emailNotificacionesPago;

    @Column(name = "email_password_app")
    private String emailPasswordApp;

    // ==================== HORARIOS ====================

    @Column(name = "horario_inicio")
    private LocalTime horarioInicio;

    @Column(name = "horario_fin")
    private LocalTime horarioFin;

    @Column(name = "mensaje_fuera_horario", columnDefinition = "TEXT")
    private String mensajeFueraHorario;

    // ==================== AUTO-RESPUESTA ====================

    /** Cuando esta activo, el bot responde automaticamente a mensajes sobre perfumes */
    @Column(name = "auto_respuesta")
    @Builder.Default
    private Boolean autoRespuesta = false;

    // ==================== ESTADO ====================

    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ==================== RELACIÓN ====================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private Empresa empresa;
}
