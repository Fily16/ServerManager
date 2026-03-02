package org.example.servermanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.servermanager.entity.ConfiguracionBot;
import org.example.servermanager.entity.Usuario;
import org.example.servermanager.repository.ConfiguracionBotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final ConfiguracionBotRepository configuracionBotRepository;

    public JwtService(ConfiguracionBotRepository configuracionBotRepository) {
        this.configuracionBotRepository = configuracionBotRepository;
    }

    public String generateToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("empresaId", usuario.getEmpresa().getId());
        claims.put("empresaNombre", usuario.getEmpresa().getNombre());
        claims.put("nombre", usuario.getNombre());

        // Obtener instancia de WhatsApp
        configuracionBotRepository.findByEmpresaId(usuario.getEmpresa().getId())
                .ifPresent(config -> claims.put("instanceName", config.getEvolutionInstancia()));

        return Jwts.builder()
                .claims(claims)
                .subject(usuario.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractEmpresaId(String token) {
        return extractClaim(token, claims -> claims.get("empresaId", Long.class));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
