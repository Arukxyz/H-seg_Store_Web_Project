package pe.edu.utp.hoseg.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.utp.hoseg.web.dto.CompraRegistrada;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Los correos del portal (reparto, bloque 4: Bienvenida, Pedido registrado y
 * Recuperar contraseña). Aquí solo se redactan; CorreoService los envía.
 */
@Service
public class NotificacionService {

    private final CorreoService correoService;
    private final String urlPublica;
    private final String rutaConsulta;

    public NotificacionService(CorreoService correoService,
                               @Value("${hoseg.url-publica}") String urlPublica,
                               @Value("${hoseg.consulta.ruta}") String rutaConsulta) {
        this.correoService = correoService;
        this.urlPublica = urlPublica;
        this.rutaConsulta = rutaConsulta;
    }

    public void bienvenida(String email, String nombre) {
        correoService.enviar(email, "Bienvenida a HÖSÉG", """
                Hola %s:

                Tu cuenta en HÖSÉG está lista. Desde "Mi cuenta" verás tus pedidos y
                cada abrigo o árbol que generen tus compras, hasta la comunidad que lo recibe.

                %s
                """.formatted(nombre, urlPublica));
    }

    public void pedidoRegistrado(String email, String nombre, CompraRegistrada compra) {
        String impacto = compra.impacto().tieneImpacto()
                ? "\nCon esta compra generas " + compra.impacto().getTexto() + " para Cusco.\n" : "";
        correoService.enviar(email, "Pedido " + compra.codigoComprobante() + " registrado", """
                Hola %s:

                Registramos tu pedido %s por S/ %s (productos S/ %s + envío S/ %s).
                Está pendiente de confirmación de pago: en cuanto lo confirmemos, lo preparamos.
                %s
                Sigue tu impacto: %s%s?boleta=%s
                """.formatted(nombre, compra.codigoComprobante(), soles(compra.totalAPagar()),
                soles(compra.totalProductos()), soles(compra.costoEnvio()), impacto,
                urlPublica, rutaConsulta, compra.codigoComprobante()));
    }

    /** El enlace vence en 1 hora y sirve una sola vez. */
    public void recuperarContrasena(String email, String nombre, String token) {
        correoService.enviar(email, "Restablece tu contraseña de HÖSÉG", """
                Hola %s:

                Para elegir una contraseña nueva, abre este enlace (vence en 1 hora y sirve una sola vez):
                %s/restablecer?token=%s

                Si no lo pediste, ignora este correo: tu contraseña no cambia.
                """.formatted(nombre, urlPublica, token));
    }

    /** Acuse de recibo del formulario de contacto (CU-08). */
    public void acuseContacto(String email, String nombre, String asunto) {
        correoService.enviar(email, "Recibimos tu mensaje: " + asunto, """
                Hola %s:

                Recibimos tu mensaje y te responderemos pronto a este correo.

                Equipo HÖSÉG
                """.formatted(nombre));
    }

    /** Constancia del Libro de Reclamaciones con su número de hoja (CU-24). */
    public void constanciaReclamo(String email, String nombre, String numeroHoja, String tipo) {
        correoService.enviar(email, "Libro de Reclamaciones: hoja " + numeroHoja, """
                Hola %s:

                Registramos tu %s con el número de hoja %s. Guarda este número:
                lo necesitarás para cualquier seguimiento. Te responderemos a este correo.

                Equipo HÖSÉG
                """.formatted(nombre, tipo, numeroHoja));
    }

    /** Respuesta del administrador a una hoja del Libro de Reclamaciones. */
    public void respuestaReclamo(String email, String nombre, String numeroHoja, String respuesta) {
        correoService.enviar(email, "Respuesta a tu hoja de reclamación " + numeroHoja, """
                Hola %s:

                Esta es nuestra respuesta a tu hoja %s:

                %s

                Equipo HÖSÉG
                """.formatted(nombre, numeroHoja, respuesta));
    }

    private static String soles(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
