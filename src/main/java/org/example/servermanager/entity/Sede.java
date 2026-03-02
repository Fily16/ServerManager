package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sede")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String direccion;

    @Column(length = 100)
    private String distrito;

    @Column(length = 100)
    private String ciudad;

    private String referencia;

    @Column(length = 20)
    private String telefono;

    @Column(name = "horario_atencion")
    private String horarioAtencion;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "es_principal")
    @Builder.Default
    private Boolean esPrincipal = false;

    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // ==================== RELACIÓN ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}
