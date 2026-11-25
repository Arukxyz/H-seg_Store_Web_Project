package pe.edu.utp.hoseg.web.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Verifica contraseñas de usuario (personal interno) igual que HashUtil del
 * escritorio: SHA-256(salt + contraseña) en hexadecimal. Como el salt vive en
 * otra columna, UsuarioDetailsService entrega la contraseña guardada como
 * "salt:hash". La web nunca guarda contraseñas de usuario (las crea el escritorio).
 */
public class Sha256ConSaltPasswordEncoder implements PasswordEncoder {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    public static String guardada(String salt, String hash) {
        return salt + ":" + hash;
    }

    /**
     * Genera "salt:hash" con un salt aleatorio de 16 bytes, como HashUtil del
     * escritorio. Spring Security lo usa al primer login para que un usuario
     * inexistente tarde lo mismo en rechazarse (evita adivinar usuarios por el
     * tiempo de respuesta); nada de esto se guarda en la base.
     */
    @Override
    public String encode(CharSequence contrasena) {
        byte[] bytes = new byte[16];
        ALEATORIO.nextBytes(bytes);
        String salt = HexFormat.of().formatHex(bytes);
        return guardada(salt, sha256Hex(salt + contrasena));
    }

    @Override
    public boolean matches(CharSequence contrasena, String guardada) {
        if (contrasena == null || guardada == null) {
            return false;
        }
        int separador = guardada.indexOf(':');
        if (separador < 0) {
            return false;
        }
        String salt = guardada.substring(0, separador);
        String hash = guardada.substring(separador + 1);
        byte[] calculado = sha256Hex(salt + contrasena).getBytes(StandardCharsets.US_ASCII);
        // Comparación en tiempo constante: no revela cuántos caracteres coinciden.
        return MessageDigest.isEqual(calculado, hash.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    static String sha256Hex(String texto) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
