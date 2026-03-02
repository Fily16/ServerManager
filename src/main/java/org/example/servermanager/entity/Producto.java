package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "precio_oferta", precision = 10, scale = 2)
    private BigDecimal precioOferta;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "tiene_stock")
    @Builder.Default
    private Boolean tieneStock = false;

    @Column(name = "stock_actual")
    @Builder.Default
    private Integer stockActual = 0;

    @Column(name = "es_destacado")
    @Builder.Default
    private Boolean esDestacado = false;

    @Column(length = 500)
    private String tags;

    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // ==================== RELACIONES ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaProducto categoria;
}
