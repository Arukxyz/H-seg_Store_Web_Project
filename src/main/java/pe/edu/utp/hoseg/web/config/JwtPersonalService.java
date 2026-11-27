package pe.edu.utp.hoseg.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import pe.edu.utp.hoseg.web.model.RolUsuario;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Emite el JWT del personal. La vigencia es la jornada de cada rol: pasado ese
 * tiempo el token vence (claim "exp") y hay que volver a iniciar sesión.
 */
@Component
public class JwtPersonalService {

    private final JwtEncoder codificador;
    private final long horasAdministrador;
    private final long horasLogistica;
    private final long horasGestorImpacto;

    public JwtPersonalService(JwtEncoder codificador,
            @Value("${hoseg.jwt.personal.horas-administrador:8}") long horasAdministrador,
            @Value("${hoseg.jwt.personal.horas-logistica:8}") long horasLogistica,
            @Value("${hoseg.jwt.personal.horas-gestor-impacto:8}") long horasGestorImpacto) {
        this.codificador = codificador;
        this.horasAdministrador = horasAdministrador;
        this.horasLogistica = horasLogistica;
        this.horasGestorImpacto = horasGestorImpacto;
    }

    /** Token emitido: el valor, cuándo vence y cuántos segundos dura. */
    public record TokenEmitido(String token, Instant expiraEn, long segundos) {
    }

    public TokenEmitido emitir(AdministradorAutenticado personal) {
        Instant ahora = Instant.now();
        Duration vigencia = Duration.ofHours(horasDe(personal.rol()));
        Instant vence = ahora.plus(vigencia);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ApiSecurityConfig.EMISOR)
                .subject(personal.username())
                .issuedAt(ahora)
                .expiresAt(vence)
                .claim("roles", List.of(personal.rol().name()))
                .claim("nombre", personal.nombre())
                .claim("uid", personal.id())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = codificador.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
        return new TokenEmitido(token, vence, vigencia.toSeconds());
    }

    private long horasDe(RolUsuario rol) {
        return switch (rol) {
            case ADMINISTRADOR -> horasAdministrador;
            case LOGISTICA -> horasLogistica;
            case GESTOR_IMPACTO -> horasGestorImpacto;
            default -> throw new IllegalArgumentException("El rol " + rol + " no usa el portal web");
        };
    }
}
