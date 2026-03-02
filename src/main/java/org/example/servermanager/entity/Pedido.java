package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.EstadoPedido;
import org.example.servermanager.enums.MetodoPago;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telefono_cliente", nullable = false, length = 20)
    private String telefonoCliente;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Column(name = "direccion_envio", length = 500)
    private String direccionEnvio;

    // ==================== MONTOS ====================

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "costo_envio", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "monto_unico", precision = 10, scale = 2)
    private BigDecimal montoUnico;

    // ==================== ESTADO ====================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE_PAGO;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 50)
    private MetodoPago metodoPago;

    @Column(columnDefinition = "TEXT")
    private String notas;

    // ==================== FECHAS ====================

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    // ==================== RELACIONES ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id")
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "pedido")
    private PagoDetectado pagoDetectado;

    // ==================== MÉTODOS HELPER ====================

    public void addDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }

    public void marcarComoPagado(MetodoPago metodo) {
        this.estado = EstadoPedido.PAGADO;
        this.metodoPago = metodo;
        this.fechaPago = LocalDateTime.now();
    }

    public void calcularTotal() {
        this.subtotal = detalles.stream()
            .map(DetallePedido::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.total = this.subtotal
            .add(this.costoEnvio != null ? this.costoEnvio : BigDecimal.ZERO)
            .subtract(this.descuento != null ? this.descuento : BigDecimal.ZERO);
    }
}
