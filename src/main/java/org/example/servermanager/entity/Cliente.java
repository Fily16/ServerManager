package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"empresa_id", "telefono"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String telefono;

    private String nombre;

    private String email;

    @Column(length = 100)
    private String distrito;

    @Column(length = 500)
    private String direccion;

    @Column(name = "total_conversaciones")
    @Builder.Default
    private Integer totalConversaciones = 0;

    @Column(name = "total_pedidos")
    @Builder.Default
    private Integer totalPedidos = 0;

    @Column(name = "ultima_interaccion")
    private LocalDateTime ultimaInteraccion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // ==================== RELACIONES ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "cliente")
    @Builder.Default
    private List<Conversacion> conversaciones = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    @Builder.Default
    private List<Pedido> pedidos = new ArrayList<>();
}
