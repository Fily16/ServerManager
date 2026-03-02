package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.servermanager.enums.TipoFuenteCatalogo;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "archivo_catalogo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "nombre_original", length = 255)
    private String nombreOriginal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_fuente", length = 30)
    private TipoFuenteCatalogo tipoFuente;

    @Column(name = "url_fuente", length = 1000)
    private String urlFuente;

    @Column(name = "texto_extraido", columnDefinition = "TEXT")
    private String textoExtraido;

    @Column(name = "total_paginas")
    @Builder.Default
    private Integer totalPaginas = 0;

    @Column(name = "ruta_paginas", length = 500)
    private String rutaPaginas;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
