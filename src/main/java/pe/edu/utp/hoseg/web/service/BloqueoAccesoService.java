package pe.edu.utp.hoseg.web.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    /** Fallos desde una misma IP, en la ventana de abajo, que disparan una alerta en el log. */
    public static final int UMBRAL_ALERTA_POR_IP = 10;
    public static final int MINUTOS_VENTANA_IP = 15;

    /** Registro de seguridad: va a logs/auditoria.log (ver logback-spring.xml). */
    private static final Logger auditoria = LoggerFactory.getLogger("hoseg.auditoria");

    /**
     * Conteo de fallos por IP (Guava Cache). Vive en memoria y está acotado: como
     * mucho 10 000 IP y cada una se olvida a los 15 minutos de su primer fallo.
     */
    private final Cache<String, AtomicInteger> fallosPorIp = CacheBuilder.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(MINUTOS_VENTANA_IP, TimeUnit.MINUTES)
            .build();

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public BloqueoAccesoService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Registra el fallo y devuelve los minutos de bloqueo si con este intento quedó bloqueado. */
    public Optional<Integer> falloDeCliente(String email) {
        clienteRepository.registrarIntentoFallido(email, MAX_INTENTOS, MINUTOS_BLOQUEO);
        Optional<Integer> bloqueo = clienteRepository.minutosDeBloqueo(email);
        bloqueo.ifPresent(minutos ->
                auditoria.warn("Cuenta de cliente bloqueada {} min tras {} intentos fallidos.", minutos, MAX_INTENTOS));
        return bloqueo;
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
        Optional<Integer> bloqueo = usuarioRepository.minutosDeBloqueo(username);
        bloqueo.ifPresent(minutos ->
                auditoria.warn("Cuenta del panel bloqueada {} min tras {} intentos fallidos.", minutos, MAX_INTENTOS));
        return bloqueo;
    }

    public void ingresoDeUsuario(String username) {
        usuarioRepository.reiniciarIntentos(username);
    }

    @Transactional(readOnly = true)
    public Optional<Integer> minutosDeBloqueoDeUsuario(String username) {
        return usuarioRepository.minutosDeBloqueo(username);
    }

    /**
     * Auditoría de fuerza bruta repartida entre muchas cuentas: cuenta los fallos de inicio
     * de sesión por IP y deja una alerta en el log al llegar a {@link #UMBRAL_ALERTA_POR_IP}.
     * No bloquea a nadie (el bloqueo real sigue siendo por cuenta, en la base): solo deja
     * evidencia. Guarda únicamente la IP y un contador, nunca el correo ni la contraseña.
     * SUPPORTS: no abre transacción (ni toma una conexión del pool) para tocar solo memoria.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void registrarIntentoFallidoDesdeIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }
        int total = fallosPorIp.asMap().computeIfAbsent(ip, clave -> new AtomicInteger()).incrementAndGet();
        if (total == UMBRAL_ALERTA_POR_IP) {
            auditoria.warn("Posible fuerza bruta: {} inicios de sesión fallidos desde la IP {} en {} min.",
                    total, ip, MINUTOS_VENTANA_IP);
        }
    }
}