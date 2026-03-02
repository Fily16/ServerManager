package org.example.servermanager.dto.response;

import org.example.servermanager.entity.Mensaje;
import org.example.servermanager.enums.TipoContenido;
import org.example.servermanager.enums.TipoRemitente;

import java.time.LocalDateTime;

public record MensajeResponse(
        Long id,
        Long conversacionId,
        TipoRemitente tipoRemitente,
        String contenido,
        TipoContenido tipoContenido,
        String mediaUrl,
        String whatsappMessageId,
        Integer tokensUsados,
        LocalDateTime fechaEnvio,
        Boolean leido
) {
    public static MensajeResponse fromEntity(Mensaje mensaje) {
        return new MensajeResponse(
                mensaje.getId(),
                mensaje.getConversacion().getId(),
                mensaje.getTipoRemitente(),
                mensaje.getContenido(),
                mensaje.getTipoContenido(),
                mensaje.getMediaUrl(),
                mensaje.getWhatsappMessageId(),
                mensaje.getTokensUsados(),
                mensaje.getFechaEnvio(),
                mensaje.getLeido()
        );
    }
}
