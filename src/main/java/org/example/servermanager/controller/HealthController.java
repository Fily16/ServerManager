package org.example.servermanager.controller;

import org.example.servermanager.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "service", "ServerManager"
        );
        return ResponseEntity.ok(ApiResponse.success(data, "OK"));
    }
}
