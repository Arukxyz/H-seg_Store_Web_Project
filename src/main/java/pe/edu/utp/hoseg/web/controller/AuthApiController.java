package pe.edu.utp.hoseg.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.config.JwtPersonalService;
import pe.edu.utp.hoseg.web.config.JwtPersonalService.TokenEmitido;
import pe.edu.utp.hoseg.web.config.Sha256ConSaltPasswordEncoder;
import pe.edu.utp.hoseg.web.model.RolUsuario;
import pe.edu.utp.hoseg.web.service.BloqueoAccesoService;
import pe.edu.utp.hoseg.web.service.UsuarioDetailsService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Autenticación del personal con JWT: POST /auth/login.
 *
 * Reutiliza lo que ya existe: la tabla usuario (UsuarioDetailsService), la
 * verificación SHA-256 con salt del escritorio y el bloqueo por intentos
 * fallidos (BloqueoAccesoService). Solo cambia lo que se entrega al final:
 * en vez de una sesión, un token con vencimiento.
 */
@RestController
@RequestMapping("/auth")
public class AuthApiController {

    private final AuthenticationManager gestor;
    private final BloqueoAccesoService bloqueoAccesoService;
    // ObjectProvider: el bean se pide solo al iniciar sesión, así los @WebMvcTest
    // existentes (VistasTest) cargan sin tener que simularlo.
    private final ObjectProvider<JwtPersonalService> jwtPersonalService;

    public AuthApiController(UsuarioDetailsService usuarioDetailsService,
            BloqueoAccesoService bloqueoAccesoService,
            ObjectProvider<JwtPersonalService> jwtPersonalService) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(new Sha256ConSaltPasswordEncoder());
        proveedor.setUserDetailsService(usuarioDetailsService);
        this.gestor = new ProviderManager(proveedor);
        this.bloqueoAccesoService = bloqueoAccesoService;
        this.jwtPersonalService = jwtPersonalService;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginRespuesta(String token, String tipo, long expiraEnSegundos, Instant expiraEn,
            String usuario, String nombre, String rol) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest peticion, HttpServletRequest request) {
        String username = peticion.username().trim();
        Authentication autenticacion;
        try {
            autenticacion = gestor.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, peticion.password()));
        } catch (LockedException e) {
            return bloqueado(bloqueoAccesoService.minutosDeBloqueoDeUsuario(username));
        } catch (DisabledException e) {
            return error(HttpStatus.FORBIDDEN, "Usuario inactivo.");
        } catch (AuthenticationException e) {
            Optional<Integer> bloqueo = bloqueoAccesoService.falloDeUsuario(username);
            // Capa adicional (Guava, solo auditoría): fuerza bruta distribuida por IP, igual que en los formularios.
            bloqueoAccesoService.registrarIntentoFallidoDesdeIp(request.getRemoteAddr());
            return bloqueo.isPresent()
                    ? bloqueado(bloqueo)
                    : error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
        }

        AdministradorAutenticado personal = (AdministradorAutenticado) autenticacion.getPrincipal();
        // El ENCARGADO solo usa el escritorio: no recibe token del portal.
        if (personal.rol() == RolUsuario.ENCARGADO) {
            return error(HttpStatus.FORBIDDEN, "Tu rol no tiene acceso al portal web.");
        }

        bloqueoAccesoService.ingresoDeUsuario(personal.username());
        TokenEmitido emitido = jwtPersonalService.getObject().emitir(personal);
        return ResponseEntity.ok(new LoginRespuesta(emitido.token(), "Bearer", emitido.segundos(),
                emitido.expiraEn(), personal.username(), personal.nombre(), personal.rol().name()));
    }

    private static ResponseEntity<Map<String, Object>> bloqueado(Optional<Integer> minutos) {
        String detalle = minutos.map(m -> "Cuenta bloqueada. Intenta de nuevo en " + m + " minutos.")
                .orElse("Cuenta bloqueada temporalmente.");
        return error(HttpStatus.LOCKED, detalle);
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}
