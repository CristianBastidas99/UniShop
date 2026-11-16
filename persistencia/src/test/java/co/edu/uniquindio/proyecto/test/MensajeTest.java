package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.MensajeRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class MensajeTest extends CrudTestBase {

    @Autowired
    private MensajeRepo mensajeRepo;

    @Autowired
    private ChatRepo chatRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Test
    public void registrarMensajeTest() {
        Mensaje m = new Mensaje();
        m.setMensaje("Me lo puede enviar hoy?");
        m.setFecha(new Date());
        m.setChat(chatRepo.findById(3L).orElseThrow());
        m.setRemitente(usuarioRepo.findById(6L).orElseThrow());

        Mensaje guardado = mensajeRepo.saveAndFlush(m);
        sincronizar();

        Mensaje buscado = mensajeRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals("Me lo puede enviar hoy?", buscado.getMensaje());
        Assertions.assertEquals(3L, buscado.getChat().getId());
        Assertions.assertEquals(6L, buscado.getRemitente().getId());
    }

    @Test
    public void remitenteDeMensajeSqlTest() {
        // El mensaje 2 del chat 1 lo escribe el vendedor del producto (usuario 7)
        Assertions.assertEquals(7L, mensajeRepo.findById(2L).orElseThrow().getRemitente().getId());
    }

    @Test
    public void eliminarMensajeTest() {
        mensajeRepo.deleteById(1L);
        sincronizar();

        Assertions.assertTrue(mensajeRepo.findById(1L).isEmpty());
        Assertions.assertEquals(3, mensajeRepo.count());
    }

    @Test
    public void actualizarMensajeTest() {
        Mensaje m = mensajeRepo.findById(3L).orElseThrow();
        m.setMensaje("Cual es el precio final con descuento?");
        mensajeRepo.saveAndFlush(m);
        sincronizar();

        Assertions.assertEquals("Cual es el precio final con descuento?", mensajeRepo.findById(3L).orElseThrow().getMensaje());
    }

    @Test
    public void listarMensajeTest() {
        List<Mensaje> lista = mensajeRepo.findAll();
        Assertions.assertEquals(4, lista.size());
    }
}
