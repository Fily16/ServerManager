package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.EstadoCampana;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campana")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"empresa", "mensajes"})
@EqualsAndHashCode(exclude = {"empresa", "mensajes"})
public class Campana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "group_jid", nullable = false)
    private String groupJid;

    @Column(name = "group_nombre")
    private String groupNombre;

    /** Instrucciones para que la IA genere mensajes personalizados */
    @Column(name = "prompt_mensaje", columnDefinition = "TEXT", nullable = false)
    private String promptMensaje;

    /** Info del negocio que la IA usa para conversar (productos, precios, etc) */
    @Column(name = "info_negocio", columnDefinition = "TEXT")
    private String infoNegocio;

    /** Link de Excel/Drive con precios */
    @Column(name = "link_precios")
    private String linkPrecios;

    /** Beneficios de unirse al grupo */
    @Column(name = "beneficios_grupo", columnDefinition = "TEXT")
    private String beneficiosGrupo;

    /** Link de invitacion al grupo de WhatsApp destino */
    @Column(name = "link_grupo_invitacion")
    private String linkGrupoInvitacion;

    /** Segundos entre cada envio (ej: 60 = 1 min, 180 = 3 min) */
    @Column(name = "delay_segundos", nullable = false)
    @Builder.Default
    private Integer delaySegundos = 120;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoCampana estado = EstadoCampana.CREADA;

    @Column(name = "total_contactos")
    @Builder.Default
    private Integer totalContactos = 0;

    @Column(name = "total_enviados")
    @Builder.Default
    private Integer totalEnviados = 0;

    @Column(name = "total_fallidos")
    @Builder.Default
    private Integer totalFallidos = 0;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "campana", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MensajeCampana> mensajes = new ArrayList<>();
}
