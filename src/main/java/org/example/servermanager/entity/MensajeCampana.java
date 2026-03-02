package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.EstadoMensajeCampana;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje_campana")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "campana")
@EqualsAndHashCode(exclude = "campana")
public class MensajeCampana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campana_id", nullable = false)
    private Campana campana;

    /** Numero de telefono del contacto (ej: 51903250695) */
    @Column(nullable = false)
    private String telefono;

    /** Nombre del contacto extraido del grupo */
    private String nombre;

    /** Mensaje personalizado generado por IA */
    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoMensajeCampana estado = EstadoMensajeCampana.PENDIENTE;

    /** WhatsApp message ID retornado por Evolution API */
    @Column(name = "whatsapp_message_id")
    private String whatsappMessageId;

    @Column(name = "error_detalle")
    private String errorDetalle;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Orden de envio dentro de la campana */
    @Column(name = "orden")
    private Integer orden;
}
