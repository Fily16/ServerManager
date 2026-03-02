package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.Plataforma;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago_detectado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDetectado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Plataforma plataforma;

    @Column(name = "referencia_externa")
    private String referenciaExterna;

    @Builder.Default
    private Boolean procesado = false;

    @Column(name = "match_automatico")
    @Builder.Default
    private Boolean matchAutomatico = false;

    @CreationTimestamp
    @Column(name = "fecha_deteccion", updatable = false)
    private LocalDateTime fechaDeteccion;

    // ==================== RELACIONES ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    // ==================== MÉTODOS HELPER ====================

    public void asociarPedido(Pedido pedido) {
        this.pedido = pedido;
        this.procesado = true;
        this.matchAutomatico = true;
    }
}
