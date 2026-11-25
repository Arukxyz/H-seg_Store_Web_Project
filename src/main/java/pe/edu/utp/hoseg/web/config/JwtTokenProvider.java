package pe.edu.utp.hoseg.web.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expiracionDias;

    public JwtTokenProvider(@Value("${hoseg.jwt.secret}") String secret,
                            @Value("${hoseg.jwt.expiracion-dias:365}") long expiracionDias) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionDias = expiracionDias;
    }

    /** Genera un JWT firmado cuyo subject es el código de comprobante (ej. WEB-000126). */
    public String generarTokenComprobante(String codigoComprobante) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(codigoComprobante)
                .claim("tipo", "BOLETA_IMPACTO")
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionDias, ChronoUnit.DAYS)))
                .signWith(key)
                .compact();
    }

    /** Valida la firma y expiración del token y devuelve el código de comprobante si es válido. */
    public Optional<String> validarYExtraerComprobante(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
