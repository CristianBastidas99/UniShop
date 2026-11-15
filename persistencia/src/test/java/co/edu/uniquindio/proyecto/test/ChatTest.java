package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ChatTest extends CrudTestBase {

    @Autowired
    private ChatRepo chatRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Test
    public void registrarChatTest() {
        Chat c = new Chat();
        c.setUsuario(usuarioRepo.findById(8L).orElseThrow());
        c.setProducto(productoRepo.findById(4L).orElseThrow());

        Chat guardado = chatRepo.saveAndFlush(c);
        sincronizar();

        Chat buscado = chatRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals("Pedro Martinez", buscado.getUsuario().getNombre());
        Assertions.assertEquals("Licuadora", buscado.getProducto().getNombre());
    }

    @Test
    public void eliminarChatTest() {
        // El chat 4 no tiene mensajes
        chatRepo.deleteById(4L);
        sincronizar();

        Assertions.assertTrue(chatRepo.findById(4L).isEmpty());
        Assertions.assertEquals(3, chatRepo.count());
    }

    @Test
    public void actualizarChatTest() {
        Chat c = chatRepo.findById(2L).orElseThrow();
        c.setUsuario(usuarioRepo.findById(9L).orElseThrow());
        chatRepo.saveAndFlush(c);
        sincronizar();

        Assertions.assertEquals("Sofia Ramirez", chatRepo.findById(2L).orElseThrow().getUsuario().getNombre());
    }

    @Test
    public void listarChatTest() {
        List<Chat> lista = chatRepo.findAll();
        Assertions.assertEquals(4, lista.size());
        Assertions.assertEquals(2, chatRepo.findById(1L).orElseThrow().getMisMensajes().size());
    }
}
