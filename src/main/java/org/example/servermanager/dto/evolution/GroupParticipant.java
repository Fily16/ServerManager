package org.example.servermanager.dto.evolution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupParticipant {

    /** Formato: "51903250695@s.whatsapp.net" */
    private String id;

    private String admin;

    /** Extrae solo el numero de telefono (sin @s.whatsapp.net) */
    public String getTelefono() {
        if (id == null) return null;
        return id.replace("@s.whatsapp.net", "");
    }

    public boolean isAdmin() {
        return "admin".equals(admin) || "superadmin".equals(admin);
    }
}
