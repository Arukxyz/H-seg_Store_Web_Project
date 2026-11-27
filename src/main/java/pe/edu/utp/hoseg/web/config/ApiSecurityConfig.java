package pe.edu.utp.hoseg.web.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import pe.edu.utp.hoseg.web.model.RolUsuario;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT para el personal (ADMINISTRADOR, LOGISTICA, GESTOR_IMPACTO).
 *
 * Es una cadena de seguridad APARTE y de mayor prioridad (@Order(0)) que solo
 * atiende /auth/** y /api/personal|admin|logistica|impacto/**. No toca las
 * cadenas de SecurityConfig: el login por formulario del panel y el de los
 * clientes siguen exactamente igual, y /api/documento/** sigue público.
 *
 * Flujo: POST /auth/login -> token firmado (HS256) con "sub", "roles", "exp"
 * -> el cliente lo manda en "Authorization: Bearer ..." -> el Resource Server
 * valida firma y expiración -> los roles del claim "roles" autorizan cada ruta.
 *
 * Errores 401/403: si quien pide es un navegador (Accept: text/html) se muestra
 * la página de error del portal (templates/error.html); si es Postman o una
 * app,
 * se responde JSON.
 */
@Configuration
public class ApiSecurityConfig {

    /** Claim "iss": solo se aceptan tokens emitidos por este portal. */
    public static final String EMISOR = "hoseg-web";

    @Bean
    @Order(0)
    SecurityFilterChain cadenaApi(HttpSecurity http, JwtAuthenticationConverter convertidor) throws Exception {
        http
                .securityMatcher("/auth/**", "/api/personal/**", "/api/admin/**",
                        "/api/logistica/**", "/api/impacto/**")
                // API sin sesión ni cookies: el token viaja en la cabecera, no hay CSRF que
                // proteger.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/personal/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole(RolUsuario.ADMINISTRADOR.name())
                        .requestMatchers("/api/logistica/**").hasRole(RolUsuario.LOGISTICA.name())
                        .requestMatchers("/api/impacto/**").hasRole(RolUsuario.GESTOR_IMPACTO.name())
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidor))
                        .authenticationEntryPoint(sinAutenticar())
                        .accessDeniedHandler(sinPermiso()));
        return http.build();
    }

    /** 401: sin token, token vencido o inválido. */
    private static AuthenticationEntryPoint sinAutenticar() {
        BearerTokenAuthenticationEntryPoint bearer = new BearerTokenAuthenticationEntryPoint();
        return (request, response, error) -> {
            if (pideHtml(request)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // error.html del portal
                return;
            }
            bearer.commence(request, response, error); // 401 + cabecera WWW-Authenticate
            escribirJson(response, "Token vencido o inválido. Inicia sesión de nuevo en /auth/login.");
        };
    }

    /** 403: token válido pero el rol no alcanza para este recurso. */
    private static AccessDeniedHandler sinPermiso() {
        return (request, response, error) -> {
            if (pideHtml(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN); // error.html del portal
                return;
            }
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            escribirJson(response, "Tu rol no tiene permiso para este recurso.");
        };
    }

    /** Un navegador pide HTML; Postman y las apps piden JSON u otro tipo. */
    private static boolean pideHtml(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
    }

    private static void escribirJson(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }

    /**
     * De claims a roles: {"roles": ["ADMINISTRADOR"]} pasa a la autoridad
     * ROLE_ADMINISTRADOR.
     */
    @Bean
    JwtAuthenticationConverter convertidorDeRoles() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("roles");
        autoridades.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(autoridades);
        return convertidor;
    }

    /**
     * Valida firma, expiración y emisor. El token del QR de la boleta (HS512, sin
     * emisor) no pasa.
     */
    @Bean
    JwtDecoder jwtDecoder(@Value("${hoseg.jwt.secret}") String secreto) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(clave(secreto))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(EMISOR));
        return decodificador;
    }

    /** Firma los tokens del personal con la misma clave (HS256). */
    @Bean
    JwtEncoder jwtEncoder(@Value("${hoseg.jwt.secret}") String secreto) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(secreto)));
    }

    private static SecretKey clave(String secreto) {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
