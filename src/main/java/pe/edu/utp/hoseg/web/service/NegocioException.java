package pe.edu.utp.hoseg.web.service;

/**
 * Regla de negocio incumplida. El mensaje está pensado para el usuario: el
 * controlador lo muestra tal cual (flash "error" o junto al formulario).
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
