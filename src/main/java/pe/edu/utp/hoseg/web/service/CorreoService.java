package pe.edu.utp.hoseg.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío de correos. Si no hay SMTP configurado (spring.mail.host), Spring no
 * crea el JavaMailSender y el correo se escribe en el log: en desarrollo el
 * enlace de recuperación se copia desde la consola. Un fallo al enviar nunca
 * deshace la operación que lo pidió (la compra ya quedó registrada).
 */
@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final ObjectProvider<JavaMailSender> remitenteSmtp;
    private final String remitente;

    public CorreoService(ObjectProvider<JavaMailSender> remitenteSmtp,
                         @Value("${hoseg.correo.remitente}") String remitente) {
        this.remitenteSmtp = remitenteSmtp;
        this.remitente = remitente;
    }

    public void enviar(String para, String asunto, String cuerpo) {
        JavaMailSender smtp = remitenteSmtp.getIfAvailable();
        if (smtp == null) {
            log.info("Correo sin SMTP configurado (no se envía)\n  Para: {}\n  Asunto: {}\n{}", para, asunto, cuerpo);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(para);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            smtp.send(mensaje);
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo \"{}\" a {}: {}", asunto, para, e.getMessage());
        }
    }
}
