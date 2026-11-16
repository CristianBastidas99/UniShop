package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.MensajeServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MensajeServicioTest extends BaseServicioTest {

    @Autowired
    private MensajeServicio mensajeServicio;

    @Test
    public void enviarMensajeComoCompradorTest() throws Exception {
        Date antes = new Date();
        Mensaje m = mensajeServicio.enviarMensaje(1L, 5L, "  Me lo guarda?  ");
        sincronizar();
        Mensaje guardado = mensajeServicio.obtenerMensaje(m.getId());
        assertEquals("Me lo guarda?", guardado.getMensaje());
        assertEquals(5L, guardado.getRemitente().getId());
        assertEquals(1L, guardado.getChat().getId());
        assertNotNull(guardado.getFecha());
        assertFalse(guardado.getFecha().getTime() < antes.getTime() - 2000);
    }

    @Test
    public void enviarMensajeComoVendedorTest() throws Exception {
        Mensaje m = mensajeServicio.enviarMensaje(1L, 7L, "Claro que si");
        sincronizar();
        assertEquals(7L, mensajeServicio.obtenerMensaje(m.getId()).getRemitente().getId());
    }

    @Test
    public void enviarMensajeTerceroTest() {
        assertThrows(NegocioException.class, () -> mensajeServicio.enviarMensaje(1L, 8L, "Hola"));
    }

    @Test
    public void enviarMensajeTextoInvalidoTest() {
        assertThrows(NegocioException.class, () -> mensajeServicio.enviarMensaje(1L, 5L, "   "));
        assertThrows(NegocioException.class, () -> mensajeServicio.enviarMensaje(1L, 5L, null));
        String largo = "a".repeat(1001);
        assertThrows(NegocioException.class, () -> mensajeServicio.enviarMensaje(1L, 5L, largo));
    }

    @Test
    public void enviarMensajeTextoMaximoTest() throws Exception {
        Mensaje m = mensajeServicio.enviarMensaje(1L, 5L, "a".repeat(1000));
        sincronizar();
        assertEquals(1000, mensajeServicio.obtenerMensaje(m.getId()).getMensaje().length());
    }

    @Test
    public void enviarMensajeInexistentesTest() {
        assertThrows(NegocioException.class, () -> mensajeServicio.enviarMensaje(9999L, 5L, "Hola"));
        assertThrows(NegocioException.class, () -> mensajeServicio.enviarMensaje(null, 5L, "Hola"));
        assertThrows(NegocioException.class, () -> mensajeServicio.enviarMensaje(1L, 9999L, "Hola"));
        assertThrows(NegocioException.class, () -> mensajeServicio.enviarMensaje(1L, null, "Hola"));
    }

    @Test
    public void listarMensajesEnOrdenTest() throws Exception {
        mensajeServicio.enviarMensaje(1L, 5L, "Ultimo mensaje");
        sincronizar();
        List<Mensaje> lista = mensajeServicio.listarMensajes(1L, 5L);
        assertEquals(3, lista.size());
        assertEquals("Hola, esta disponible?", lista.get(0).getMensaje());
        assertEquals("Si, quedan 50 unidades", lista.get(1).getMensaje());
        assertEquals("Ultimo mensaje", lista.get(2).getMensaje());
        assertEquals(3, mensajeServicio.listarMensajes(1L, 7L).size());
    }

    @Test
    public void listarMensajesTerceroTest() {
        assertThrows(NegocioException.class, () -> mensajeServicio.listarMensajes(1L, 8L));
        assertThrows(NegocioException.class, () -> mensajeServicio.listarMensajes(1L, null));
        assertThrows(NegocioException.class, () -> mensajeServicio.listarMensajes(9999L, 5L));
    }

    @Test
    public void obtenerMensajeNuloTest() {
        assertThrows(NegocioException.class, () -> mensajeServicio.obtenerMensaje(null));
    }
}
