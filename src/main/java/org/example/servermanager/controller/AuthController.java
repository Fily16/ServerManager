package org.example.servermanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.servermanager.dto.response.ApiResponse;
import org.example.servermanager.dto.request.LoginRequest;
import org.example.servermanager.dto.request.RegisterRequest;
import org.example.servermanager.dto.response.AuthResponse;
import org.example.servermanager.entity.ConfiguracionBot;
import org.example.servermanager.entity.Empresa;
import org.example.servermanager.entity.Usuario;
import org.example.servermanager.exception.BusinessException;
import org.example.servermanager.exception.ResourceNotFoundException;
import org.example.servermanager.repository.ConfiguracionBotRepository;
import org.example.servermanager.repository.EmpresaRepository;
import org.example.servermanager.repository.UsuarioRepository;
import org.example.servermanager.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ConfiguracionBotRepository configuracionBotRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", request.email()));

        String token = jwtService.generateToken(usuario);
        AuthResponse authResponse = buildAuthResponse(usuario, token);

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login exitoso"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con ese email");
        }

        Empresa empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", request.empresaId()));

        Usuario usuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nombre(request.nombre())
                .empresa(empresa)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);
        AuthResponse authResponse = buildAuthResponse(usuario, token);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authResponse, "Registro exitoso"));
    }

    private AuthResponse buildAuthResponse(Usuario usuario, String token) {
        String instanceName = configuracionBotRepository.findByEmpresaId(usuario.getEmpresa().getId())
                .map(ConfiguracionBot::getEvolutionInstancia)
                .orElse("");

        return AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .empresaId(usuario.getEmpresa().getId())
                .empresaNombre(usuario.getEmpresa().getNombre())
                .instanceName(instanceName)
                .build();
    }
}
