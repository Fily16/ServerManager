package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.Plan;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, length = 11)
    private String ruc;

    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "direccion_fiscal", length = 500)
    private String direccionFiscal;

    @Builder.Default
    private Boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private Plan plan = Plan.BASICO;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ==================== RELACIONES ====================

    @OneToOne(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private ConfiguracionBot configuracionBot;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Sede> sedes = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CategoriaProducto> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Cliente> clientes = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Conversacion> conversaciones = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pedido> pedidos = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PagoDetectado> pagosDetectados = new ArrayList<>();

    // ==================== MÉTODOS HELPER ====================

    public void addSede(Sede sede) {
        sedes.add(sede);
        sede.setEmpresa(this);
    }

    public void removeSede(Sede sede) {
        sedes.remove(sede);
        sede.setEmpresa(null);
    }

    public void addProducto(Producto producto) {
        productos.add(producto);
        producto.setEmpresa(this);
    }

    public void addCategoria(CategoriaProducto categoria) {
        categorias.add(categoria);
        categoria.setEmpresa(this);
    }
}
