package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ChatServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServicioTest extends BaseServicioTest {

    @Autowired
    private ChatServicio chatServicio;
    @Autowired
    private UsuarioServicio usuarioServicio;
    @Autowired
    private ProductoServicio productoServicio;

    private Chat nuevoChat(String emailComprador, Long idProducto) throws NegocioException {
        Chat c = new Chat();
        c.setUsuario(usuarioServicio.obtenerUsuario(emailComprador));
        c.setProducto(productoServicio.obtenerProducto(idProducto));
        return c;
    }

    @Test
    public void registrarChatTest() throws Exception {
        // lorena (id 5) aún no tiene chat sobre el producto 1
        Chat guardado = chatServicio.registrarChat(nuevoChat("lorena@email.com", 1L));
        sincronizar();
        assertNotNull(guardado.getId());
        assertNotNull(chatServicio.obtenerChat(guardado.getId()));
    }

    @Test
    public void registrarChatDuplicadoTest() throws Exception {
        // en el SQL, lorena (id 5) ya tiene un chat sobre el producto 3
        Chat duplicado = nuevoChat("lorena@email.com", 3L);
        assertThrows(NegocioException.class, () -> chatServicio.registrarChat(duplicado));
    }
}
