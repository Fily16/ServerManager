package org.example.servermanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.response.MensajeResponse;
import org.example.servermanager.dto.response.PageResponse;
import org.example.servermanager.entity.Mensaje;
import org.example.servermanager.enums.TipoRemitente;
import org.example.servermanager.service.MensajeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/conversaciones/{conversacionId}/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MensajeResponse>>> listar(
            @PathVariable Long conversacionId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<Mensaje> page = mensajeService.listarPorConversacionPaginado(conversacionId, pageable);
        PageResponse<MensajeResponse> response = PageResponse.fromPage(page, MensajeResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<MensajeResponse>>> listarTodos(@PathVariable Long conversacionId) {
        List<Mensaje> mensajes = mensajeService.listarPorConversacion(conversacionId);
        List<MensajeResponse> response = mensajes.stream()
                .map(MensajeResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tipo/{tipoRemitente}")
    public ResponseEntity<ApiResponse<List<MensajeResponse>>> listarPorTipo(
            @PathVariable Long conversacionId,
            @PathVariable TipoRemitente tipoRemitente) {
        List<Mensaje> mensajes = mensajeService.obtenerPorTipo(conversacionId, tipoRemitente);
        List<MensajeResponse> response = mensajes.stream()
                .map(MensajeResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ultimos")
    public ResponseEntity<ApiResponse<List<MensajeResponse>>> obtenerUltimos(
            @PathVariable Long conversacionId,
            @RequestParam(defaultValue = "10") int limite) {
        List<Mensaje> mensajes = mensajeService.obtenerUltimos(conversacionId, limite);
        List<MensajeResponse> response = mensajes.stream()
                .map(MensajeResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/marcar-leidos")
    public ResponseEntity<ApiResponse<Integer>> marcarComoLeidos(@PathVariable Long conversacionId) {
        int actualizados = mensajeService.marcarComoLeidos(conversacionId);
        return ResponseEntity.ok(ApiResponse.success(actualizados, actualizados + " mensajes marcados como leídos"));
    }

    @GetMapping("/no-leidos/count")
    public ResponseEntity<ApiResponse<Long>> contarNoLeidos(@PathVariable Long conversacionId) {
        long count = mensajeService.contarNoLeidos(conversacionId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
