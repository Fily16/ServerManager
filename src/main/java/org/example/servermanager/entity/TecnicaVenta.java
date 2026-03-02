package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Técnicas de venta extraídas de libros como:
 * - $100M Offers (Alex Hormozi)
 * - Way of the Wolf (Jordan Belfort)
 * - SPIN Selling
 * - The Challenger Sale
 * - Never Split the Difference (Chris Voss)
 * 
 * Estas técnicas se inyectan en el prompt del bot para que venda como un experto.
 */
@Entity
@Table(name = "tecnica_venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TecnicaVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Categoría de la técnica:
     * - APERTURA (romper hielo, generar rapport)
     * - DESCUBRIMIENTO (detectar necesidades, pain points)
     * - PRESENTACION (mostrar valor, no características)
     * - OBJECIONES (manejar dudas, precio, "lo pienso")
     * - CIERRE (técnicas de cierre, urgencia)
     * - SEGUIMIENTO (recuperar leads fríos)
     */
    @Column(nullable = false, length = 50)
    private String categoria;

    /**
     * Nombre de la técnica
     */
    @Column(nullable = false, unique = true)
    private String nombre;

    /**
     * Descripción detallada de cómo aplicar la técnica
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    /**
     * Ejemplo de uso en conversación
     */
    @Column(columnDefinition = "TEXT")
    private String ejemplo;

    /**
     * Libro/Autor de origen
     */
    @Column(length = 200)
    private String fuente;

    /**
     * Orden de prioridad (menor = más importante)
     */
    @Builder.Default
    private Integer prioridad = 100;

    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
