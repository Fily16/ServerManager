package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.TipoContenido;
import org.example.servermanager.enums.TipoRemitente;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_remitente", nullable = false, length = 20)
    private TipoRemitente tipoRemitente;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", length = 50)
    @Builder.Default
    private TipoContenido tipoContenido = TipoContenido.TEXTO;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "whatsapp_message_id", length = 100)
    private String whatsappMessageId;

    @Column(name = "tokens_usados")
    private Integer tokensUsados;

    @CreationTimestamp
    @Column(name = "fecha_envio", updatable = false)
    private LocalDateTime fechaEnvio;

    @Builder.Default
    private Boolean leido = false;

    // ==================== RELACIÓN ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private Conversacion conversacion;
}
