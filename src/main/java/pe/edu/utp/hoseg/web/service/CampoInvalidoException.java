package pe.edu.utp.hoseg.web.service;

/**
 * Regla de negocio sobre un campo concreto del formulario (email ya
 * registrado, documento repetido, contraseña actual incorrecta). El
 * controlador la convierte en {@code bindingResult.rejectValue(getCampo(), ...)}
 * para que el mensaje salga junto al campo, como los de Bean Validation.
 */
public class CampoInvalidoException extends NegocioException {

    private final String campo;

    public CampoInvalidoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
