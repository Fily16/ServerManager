package org.example.servermanager.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageKey {

    /**
     * Número del remitente en formato: 51999888777@s.whatsapp.net
     */
    @JsonProperty("remoteJid")
    private String remoteJid;

    /**
     * true si el mensaje fue enviado por nosotros (el bot)
     */
    @JsonProperty("fromMe")
    private Boolean fromMe;

    /**
     * ID único del mensaje en WhatsApp
     */
    @JsonProperty("id")
    private String id;

    /**
     * Extrae solo el número de teléfono (sin @s.whatsapp.net, @lid, etc.)
     */
    public String getPhoneNumber() {
        if (remoteJid == null) return null;
        // Quitar cualquier sufijo @xxx
        return remoteJid.split("@")[0];
    }

    /**
     * Verifica si es un mensaje de grupo
     */
    public boolean isGroupMessage() {
        return remoteJid != null && remoteJid.contains("@g.us");
    }

    /**
     * Verifica si el JID es formato LID (Linked ID)
     */
    public boolean isLidFormat() {
        return remoteJid != null && remoteJid.endsWith("@lid");
    }
}
