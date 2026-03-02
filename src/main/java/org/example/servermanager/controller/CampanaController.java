package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.servermanager.dto.evolution.GroupInfo;
import org.example.servermanager.dto.evolution.GroupParticipant;
import org.example.servermanager.dto.request.CampanaRequest;
import org.example.servermanager.dto.response.CampanaResponse;
import org.example.servermanager.dto.response.MensajeCampanaResponse;
import org.example.servermanager.service.CampanaService;
import org.example.servermanager.service.EvolutionGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/campanas")
@RequiredArgsConstructor
@Slf4j
public class CampanaController {

    private final CampanaService campanaService;
    private final EvolutionGroupService evolutionGroupService;

    // ==================== GRUPOS (previo a crear campana) ====================

    /** Lista todos los grupos de WhatsApp de la empresa */
    @GetMapping("/grupos")
    public ResponseEntity<List<GroupInfo>> listarGrupos(@PathVariable Long empresaId) {
        long start = System.currentTimeMillis();
        List<GroupInfo> grupos = evolutionGroupService.listarGrupos(empresaId);
        long elapsed = System.currentTimeMillis() - start;
        log.info("GET /grupos completado: {} grupos en {}ms", grupos.size(), elapsed);
        return ResponseEntity.ok(grupos);
    }

    /** Obtiene los participantes de un grupo */
    @GetMapping("/grupos/participantes")
    public ResponseEntity<List<GroupParticipant>> obtenerParticipantes(
            @PathVariable Long empresaId,
            @RequestParam String groupJid) {
        return ResponseEntity.ok(evolutionGroupService.obtenerParticipantes(empresaId, groupJid));
    }

    // ==================== CRUD CAMPANAS ====================

    /** Crea una campana: carga contactos del grupo y genera mensajes con IA */
    @PostMapping
    public ResponseEntity<CampanaResponse> crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody CampanaRequest request) {
        return ResponseEntity.ok(campanaService.crear(empresaId, request));
    }

    /** Lista todas las campanas de la empresa */
    @GetMapping
    public ResponseEntity<List<CampanaResponse>> listar(@PathVariable Long empresaId) {
        return ResponseEntity.ok(campanaService.listarPorEmpresa(empresaId));
    }

    /** Obtiene una campana con su progreso */
    @GetMapping("/{campanaId}")
    public ResponseEntity<CampanaResponse> obtener(
            @PathVariable Long empresaId,
            @PathVariable Long campanaId) {
        return ResponseEntity.ok(campanaService.obtenerPorId(empresaId, campanaId));
    }

    /** Lista los mensajes individuales de una campana */
    @GetMapping("/{campanaId}/mensajes")
    public ResponseEntity<List<MensajeCampanaResponse>> listarMensajes(
            @PathVariable Long empresaId,
            @PathVariable Long campanaId) {
        return ResponseEntity.ok(campanaService.listarMensajes(campanaId));
    }

    // ==================== CONTROL DE CAMPANA ====================

    /** Inicia el envio */
    @PostMapping("/{campanaId}/iniciar")
    public ResponseEntity<CampanaResponse> iniciar(
            @PathVariable Long empresaId,
            @PathVariable Long campanaId) {
        return ResponseEntity.ok(campanaService.iniciar(empresaId, campanaId));
    }

    /** Pausa el envio */
    @PostMapping("/{campanaId}/pausar")
    public ResponseEntity<CampanaResponse> pausar(
            @PathVariable Long empresaId,
            @PathVariable Long campanaId) {
        return ResponseEntity.ok(campanaService.pausar(empresaId, campanaId));
    }

    /** Reanuda el envio */
    @PostMapping("/{campanaId}/reanudar")
    public ResponseEntity<CampanaResponse> reanudar(
            @PathVariable Long empresaId,
            @PathVariable Long campanaId) {
        return ResponseEntity.ok(campanaService.reanudar(empresaId, campanaId));
    }

    /** Cancela la campana */
    @PostMapping("/{campanaId}/cancelar")
    public ResponseEntity<CampanaResponse> cancelar(
            @PathVariable Long empresaId,
            @PathVariable Long campanaId) {
        return ResponseEntity.ok(campanaService.cancelar(empresaId, campanaId));
    }

    /** Cambia el delay entre mensajes (en caliente) */
    @PatchMapping("/{campanaId}/delay")
    public ResponseEntity<CampanaResponse> cambiarDelay(
            @PathVariable Long empresaId,
            @PathVariable Long campanaId,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(campanaService.cambiarDelay(
                empresaId, campanaId, body.get("delaySegundos")));
    }
}
