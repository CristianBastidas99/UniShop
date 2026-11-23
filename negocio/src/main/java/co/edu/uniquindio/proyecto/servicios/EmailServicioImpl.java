package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

/**
 * Envío síncrono de correos HTML con {@link JavaMailSender}. El remitente sale de
 * {@code unishop.correo.remitente} y acepta el formato {@code Nombre <correo>}.
 */
@Service
public class EmailServicioImpl implements EmailServicio {

    private static final Logger LOG = LoggerFactory.getLogger(EmailServicioImpl.class);

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmailServicioImpl(JavaMailSender mailSender,
                             @Value("${unishop.correo.remitente:UniShop <no-responder@unishop.com>}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarCorreo(String destinatario, String asunto, String html) throws NegocioException {
        if (vacio(destinatario)) {
            throw new NegocioException("El destinatario del correo es obligatorio");
        }
        if (vacio(asunto)) {
            throw new NegocioException("El asunto del correo es obligatorio");
        }
        if (vacio(html)) {
            throw new NegocioException("El contenido del correo es obligatorio");
        }
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(destinatario.trim());
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(mensaje);
        } catch (MailException | MessagingException e) {
            // No se registra el destinatario ni el contenido: solo el tipo de error
            LOG.error("No se pudo enviar el correo: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new NegocioException("No se pudo enviar el correo");
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
