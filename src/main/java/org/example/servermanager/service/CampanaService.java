package org.example.servermanager.service;

import org.example.servermanager.dto.request.CampanaRequest;
import org.example.servermanager.dto.response.CampanaResponse;
import org.example.servermanager.dto.response.MensajeCampanaResponse;

import java.util.List;

public interface CampanaService {

    /** Crea la campana, carga contactos del grupo y genera mensajes con IA */
    CampanaResponse crear(Long empresaId, CampanaRequest request);

    /** Lista todas las campanas de una empresa */
    List<CampanaResponse> listarPorEmpresa(Long empresaId);

    /** Obtiene una campana con su progreso actualizado */
    CampanaResponse obtenerPorId(Long empresaId, Long campanaId);

    /** Lista los mensajes individuales de una campana */
    List<MensajeCampanaResponse> listarMensajes(Long campanaId);

    /** Inicia el envio de la campana */
    CampanaResponse iniciar(Long empresaId, Long campanaId);

    /** Pausa el envio */
    CampanaResponse pausar(Long empresaId, Long campanaId);

    /** Reanuda el envio */
    CampanaResponse reanudar(Long empresaId, Long campanaId);

    /** Cancela la campana definitivamente */
    CampanaResponse cancelar(Long empresaId, Long campanaId);

    /** Cambia el delay entre mensajes (en caliente) */
    CampanaResponse cambiarDelay(Long empresaId, Long campanaId, Integer nuevoDelaySegundos);

    /** Genera el contenido de un mensaje individual con OpenAI justo antes de enviarlo */
    String generarMensajeIndividual(Long campanaId);
}
