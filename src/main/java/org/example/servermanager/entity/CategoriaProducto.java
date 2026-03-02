package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Builder.Default
    private Integer orden = 0;

    @Builder.Default
    private Boolean activo = true;

    // ==================== RELACIONES ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "categoria")
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();
}
