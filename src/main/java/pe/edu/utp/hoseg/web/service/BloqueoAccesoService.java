package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;

import java.util.Optional;

/**
 * Protección contra fuerza bruta: 3 intentos fallidos bloquean 5 minutos,
 * las mismas reglas que AuthService del escritorio. El contador y la hora
 * viven en la base, así que sobreviven a un reinicio y valen en ambos sistemas.
 */
@Service
@Transactional
public class BloqueoAccesoService {

    public static final int MAX_INTENTOS = 3;
    public static final int MINUTOS_BLOQUEO = 5;

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public BloqueoAccesoService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Registra el fallo y devuelve los minutos de bloqueo si con este intento quedó bloqueado. */
    public Optional<Integer> falloDeCliente(String email) {
        clienteRepository.registrarIntentoFallido(email, MAX_INTENTOS, MINUTOS_BLOQUEO);
        return clienteRepository.minutosDeBloqueo(email);
    }

    public void ingresoDeCliente(String email) {
        clienteRepository.reiniciarIntentos(email);
    }

    @Transactional(readOnly = true)
    public Optional<Integer> minutosDeBloqueoDeCliente(String email) {
        return clienteRepository.minutosDeBloqueo(email);
    }

    public Optional<Integer> falloDeUsuario(String username) {
        usuarioRepository.registrarIntentoFallido(username, MAX_INTENTOS, MINUTOS_BLOQUEO);
        return usuarioRepository.minutosDeBloqueo(username);
    }

    public void ingresoDeUsuario(String username) {
        usuarioRepository.reiniciarIntentos(username);
    }

    @Transactional(readOnly = true)
    public Optional<Integer> minutosDeBloqueoDeUsuario(String username) {
        return usuarioRepository.minutosDeBloqueo(username);
    }
}
