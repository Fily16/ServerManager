package org.example.servermanager.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.example.servermanager.enums.TonoConversacion;

import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfiguracionBotRequest(
        @Size(max = 500, message = "La URL de Evolution API no puede exceder 500 caracteres")
        String evolutionApiUrl,

        String evolutionApiKey,

        @Size(max = 100, message = "La instancia no puede exceder 100 caracteres")
        String evolutionInstancia,

        @Size(max = 20, message = "El número de WhatsApp no puede exceder 20 caracteres")
        String numeroWhatsapp,

        @Size(max = 100, message = "El nombre del bot no puede exceder 100 caracteres")
        String nombreBot,

        String mensajeBienvenida,

        String promptSistema,

        TonoConversacion tonoConversacion,

        @Size(max = 50, message = "El modelo AI no puede exceder 50 caracteres")
        String modeloAi,

        Boolean verificacionPagosActivo,

        @Email(message = "El email de notificaciones debe ser válido")
        String emailNotificacionesPago,

        String emailPasswordApp,

        String linkGrupoConsolidado,

        String linkCatalogo,

        String tiempoEntrega,

        String linkTiktok,

        String promptCampana,

        LocalTime horarioInicio,

        LocalTime horarioFin,

        String mensajeFueraHorario,

        Boolean autoRespuesta,

        Boolean activo
) {}
