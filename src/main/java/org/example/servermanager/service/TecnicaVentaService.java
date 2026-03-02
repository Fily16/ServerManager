package org.example.servermanager.service;

import org.example.servermanager.dto.request.TecnicaVentaRequest;
import org.example.servermanager.dto.response.TecnicaVentaResponse;

import java.util.List;

public interface TecnicaVentaService {

    List<TecnicaVentaResponse> listarTodas();

    List<TecnicaVentaResponse> listarActivas();

    List<TecnicaVentaResponse> listarPorCategoria(String categoria);

    List<String> listarCategorias();

    TecnicaVentaResponse obtenerPorId(Long id);

    TecnicaVentaResponse crear(TecnicaVentaRequest request);

    List<TecnicaVentaResponse> crearMasivo(List<TecnicaVentaRequest> requests);

    TecnicaVentaResponse actualizar(Long id, TecnicaVentaRequest request);

    void activar(Long id);

    void desactivar(Long id);

    void eliminar(Long id);
}
