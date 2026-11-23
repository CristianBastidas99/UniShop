package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.correo.CorreoPendienteEvent;
import co.edu.uniquindio.proyecto.correo.CorreoPendienteListener;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.EmailServicio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Pruebas unitarias (sin Spring) del listener que envía los correos pendientes. */
public class CorreoPendienteListenerTest {

    private final CorreoPendienteEvent evento = new CorreoPendienteEvent("ana@email.com", "Asunto", "<p>x</p>");

    @Test
    public void llamaAlServicioConLosDatosDelEventoTest() throws NegocioException {
        EmailServicio email = Mockito.mock(EmailServicio.class);
        new CorreoPendienteListener(email).enviar(evento);
        Mockito.verify(email).enviarCorreo("ana@email.com", "Asunto", "<p>x</p>");
    }

    @Test
    public void unFalloNoSePropagaTest() throws NegocioException {
        EmailServicio email = Mockito.mock(EmailServicio.class);
        Mockito.doThrow(new NegocioException("No se pudo enviar el correo"))
                .when(email).enviarCorreo(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Assertions.assertDoesNotThrow(() -> new CorreoPendienteListener(email).enviar(evento));

        Mockito.doThrow(new IllegalStateException("inesperado"))
                .when(email).enviarCorreo(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Assertions.assertDoesNotThrow(() -> new CorreoPendienteListener(email).enviar(evento));
    }
}
