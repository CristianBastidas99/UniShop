package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.MensajeRepo;
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

    @Test
    public void registrarMensajeTest() {
        Mensaje m = new Mensaje();
        m.setMensaje("Me lo puede enviar hoy?");
        m.setFecha(new Date());
        m.setChat(chatRepo.findById(3L).orElseThrow());

        Mensaje guardado = mensajeRepo.saveAndFlush(m);
        sincronizar();

        Mensaje buscado = mensajeRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals("Me lo puede enviar hoy?", buscado.getMensaje());
        Assertions.assertEquals(3L, buscado.getChat().getId());
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
