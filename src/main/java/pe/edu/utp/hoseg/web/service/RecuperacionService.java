package pe.edu.utp.hoseg.web.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.RestablecerContrasenaForm;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.model.TokenRecuperacion;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;
import pe.edu.utp.hoseg.web.repository.TokenRecuperacionRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Recuperar contraseña (CU-12). El enlace lleva un token aleatorio de un solo
 * uso que vence en 1 hora; en la base solo queda su SHA-256. La respuesta al
 * pedirlo es la misma exista o no el email, para no revelar quién es cliente.
 */
@Service
@Transactional(readOnly = true)
public class RecuperacionService {

    private static final int MINUTOS_VIGENCIA = 60;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final ClienteRepository clienteRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder codificadorContrasena;
    private final NotificacionService notificacionService;

    public RecuperacionService(ClienteRepository clienteRepository,
                               TokenRecuperacionRepository tokenRepository,
                               PasswordEncoder codificadorContrasena,
                               NotificacionService notificacionService) {
        this.clienteRepository = clienteRepository;
        this.tokenRepository = tokenRepository;
        this.codificadorContrasena = codificadorContrasena;
        this.notificacionService = notificacionService;
    }

    /**
     * Envía el enlace si el email es de un cliente con cuenta. Los compradores
     * históricos sin contraseña no reciben nada: primero deben registrarse.
     * Pedir un enlace nuevo invalida los anteriores.
     */
    @Transactional
    public void solicitar(String email) {
        clienteRepository.findByEmailIgnoreCase(email.trim())
                .filter(Cliente::puedeIniciarSesion)
                .ifPresent(cliente -> {
                    tokenRepository.invalidarPendientes(cliente.getId());
                    String token = nuevoToken();
                    TokenRecuperacion registro = new TokenRecuperacion();
                    registro.setCliente(cliente);
                    registro.setTokenHash(sha256(token));
                    registro.setExpiraEn(OffsetDateTime.now().plusMinutes(MINUTOS_VIGENCIA));
                    tokenRepository.save(registro);
                    notificacionService.recuperarContrasena(cliente.getEmail(), cliente.getNombre(), token);
                });
    }

    /** Para "Nueva contraseña": false muestra la variante "Enlace vencido" (2.4). */
    public boolean tokenVigente(String token) {
        return token != null && !token.isBlank() && tokenRepository.buscarVigente(sha256(token)).isPresent();
    }

    /**
     * Cambia la contraseña con un enlace vigente, lo marca como usado y
     * desbloquea la cuenta (quien recupera el acceso no queda bloqueado).
     *
     * @throws NegocioException si el enlace venció o ya se usó
     */
    @Transactional
    public void restablecer(RestablecerContrasenaForm form) {
        TokenRecuperacion registro = tokenRepository.buscarVigente(sha256(form.getToken()))
                .orElseThrow(() -> new NegocioException("El enlace venció o ya se usó. Pide uno nuevo."));
        Cliente cliente = registro.getCliente();
        cliente.setPasswordHash(codificadorContrasena.encode(form.getNueva()));
        tokenRepository.invalidarPendientes(cliente.getId());
        clienteRepository.reiniciarIntentos(cliente.getEmail());
    }

    /** 32 bytes aleatorios en Base64 apto para URL (43 caracteres). */
    private static String nuevoToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** token_recuperacion.token_hash: 64 caracteres hexadecimales en minúscula. */
    static String sha256(String token) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
