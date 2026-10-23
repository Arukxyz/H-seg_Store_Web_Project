package pe.edu.utp.hoseg.web.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Verifica contraseñas de usuario (personal interno) igual que HashUtil del
 * escritorio: SHA-256(salt + contraseña) en hexadecimal. Como el salt vive en
 * otra columna, UsuarioDetailsService entrega la contraseña guardada como
 * "salt:hash". Solo verifica: los usuarios los crea el escritorio.
 */
public class Sha256ConSaltPasswordEncoder implements PasswordEncoder {

    public static String guardada(String salt, String hash) {
        return salt + ":" + hash;
    }

    @Override
    public String encode(CharSequence contrasena) {
        throw new UnsupportedOperationException("Los usuarios internos se crean desde el escritorio.");
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
