package org.example.servermanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_saas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSaas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String clave;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(length = 500)
    private String descripcion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ==================== CLAVES PREDEFINIDAS ====================
    
    public static final String OPENAI_API_KEY = "OPENAI_API_KEY";
    public static final String OPENAI_MODELO_DEFAULT = "OPENAI_MODELO_DEFAULT";
    public static final String OPENAI_MAX_TOKENS = "OPENAI_MAX_TOKENS";

    public static final String ELEVENLABS_API_KEY = "ELEVENLABS_API_KEY";
    public static final String ELEVENLABS_VOICE_ID = "ELEVENLABS_VOICE_ID";
}
