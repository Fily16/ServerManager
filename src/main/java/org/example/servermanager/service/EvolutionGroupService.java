package org.example.servermanager.service;

import org.example.servermanager.dto.evolution.GroupInfo;
import org.example.servermanager.dto.evolution.GroupParticipant;

import java.util.List;

public interface EvolutionGroupService {

    /** Lista todos los grupos de WhatsApp de la instancia de una empresa */
    List<GroupInfo> listarGrupos(Long empresaId);

    /** Obtiene los participantes de un grupo especifico */
    List<GroupParticipant> obtenerParticipantes(Long empresaId, String groupJid);
}
