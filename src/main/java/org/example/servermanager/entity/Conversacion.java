package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.EstadoConversacion;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telefono_cliente", nullable = false, length = 20)
    private String telefonoCliente;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private EstadoConversacion estado = EstadoConversacion.ACTIVA;

    @Column(name = "contexto_ai", columnDefinition = "TEXT")
    private String contextoAi;

    @Column(name = "total_mensajes")
    @Builder.Default
    private Integer totalMensajes = 0;

    @CreationTimestamp
    @Column(name = "fecha_inicio", updatable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_ultimo_mensaje")
    private LocalDateTime fechaUltimoMensaje;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    // ==================== RELACIONES ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Mensaje> mensajes = new ArrayList<>();

    @OneToMany(mappedBy = "conversacion")
    @Builder.Default
    private List<Pedido> pedidos = new ArrayList<>();

    // ==================== MÉTODOS HELPER ====================

    public void addMensaje(Mensaje mensaje) {
        mensajes.add(mensaje);
        mensaje.setConversacion(this);
        this.totalMensajes++;
        this.fechaUltimoMensaje = LocalDateTime.now();
    }

    public void cerrar() {
        this.estado = EstadoConversacion.CERRADA;
        this.fechaCierre = LocalDateTime.now();
    }
}
