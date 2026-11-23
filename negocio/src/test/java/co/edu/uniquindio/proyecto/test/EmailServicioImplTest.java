package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.EmailServicioImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/** Pruebas unitarias (sin Spring) del envío de correos con un {@link JavaMailSender} simulado. */
public class EmailServicioImplTest {

    private JavaMailSender sender;
    private MimeMessage mensaje;
    private EmailServicioImpl servicio;

    @BeforeEach
    public void preparar() {
        sender = Mockito.mock(JavaMailSender.class);
        mensaje = new MimeMessage(Session.getInstance(new Properties()));
        Mockito.when(sender.createMimeMessage()).thenReturn(mensaje);
        servicio = new EmailServicioImpl(sender, "UniShop <no-responder@unishop.com>");
    }

    @Test
    public void enviaCorreoHtmlUtf8Test() throws Exception {
        servicio.enviarCorreo("ana@email.com", "Tu compra #1 en UniShop", "<p>Niño, café</p>");

        Mockito.verify(sender).send(mensaje);
        mensaje.saveChanges();
        Assertions.assertEquals("ana@email.com",
                ((InternetAddress) mensaje.getRecipients(Message.RecipientType.TO)[0]).getAddress());
        Assertions.assertEquals("Tu compra #1 en UniShop", mensaje.getSubject());
        InternetAddress remitente = (InternetAddress) mensaje.getFrom()[0];
        Assertions.assertEquals("no-responder@unishop.com", remitente.getAddress());
        Assertions.assertEquals("UniShop", remitente.getPersonal());
        String tipo = mensaje.getContentType().toLowerCase();
        Assertions.assertTrue(tipo.contains("text/html"), tipo);
        Assertions.assertTrue(tipo.contains("utf-8"), tipo);
        Assertions.assertEquals("<p>Niño, café</p>", mensaje.getContent());
    }

    @Test
    public void validaCamposVaciosTest() {
        Assertions.assertThrows(NegocioException.class, () -> servicio.enviarCorreo(null, "a", "<p>x</p>"));
        Assertions.assertThrows(NegocioException.class, () -> servicio.enviarCorreo("  ", "a", "<p>x</p>"));
        Assertions.assertThrows(NegocioException.class, () -> servicio.enviarCorreo("a@b.com", "", "<p>x</p>"));
        Assertions.assertThrows(NegocioException.class, () -> servicio.enviarCorreo("a@b.com", "a", " "));
        Mockito.verify(sender, Mockito.never()).send(Mockito.any(MimeMessage.class));
    }

    @Test
    public void errorDelServidorSeConvierteEnNegocioExceptionTest() {
        Mockito.doThrow(new MailSendException("servidor caído")).when(sender).send(Mockito.any(MimeMessage.class));
        NegocioException e = Assertions.assertThrows(NegocioException.class,
                () -> servicio.enviarCorreo("ana@email.com", "Asunto", "<p>x</p>"));
        Assertions.assertEquals("No se pudo enviar el correo", e.getMessage());
    }
}
