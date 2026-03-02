package org.example.servermanager.service;

import org.example.servermanager.entity.ConfiguracionBot;
import org.example.servermanager.entity.Conversacion;
import org.example.servermanager.entity.Producto;

import java.util.List;

public interface OpenAIService {

    /**
     * Genera una respuesta de venta inteligente
     * 
     * @param config Configuración del bot de la empresa
     * @param conversacion Conversación actual
     * @param mensajeCliente Último mensaje del cliente
     * @param productos Lista de productos de la empresa
     * @return Respuesta generada y tokens usados
     */
    OpenAIResponse generarRespuestaVenta(
            ConfiguracionBot config,
            Conversacion conversacion,
            String mensajeCliente,
            List<Producto> productos
    );

    /**
     * Clasifica si un mensaje es sobre perfumes/fragancias/productos de belleza.
     * Usado para filtrar mensajes irrelevantes (ej: familiares, spam).
     */
    boolean esConsultaSobrePerfumes(String mensaje);

    /**
     * Respuesta con información de tokens
     */
    record OpenAIResponse(String texto, Integer tokensUsados) {}
}
