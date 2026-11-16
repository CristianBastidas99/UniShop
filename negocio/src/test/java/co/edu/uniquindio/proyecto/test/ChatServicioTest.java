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

    @Test
    public void obtenerOCrearChatNuevoTest() throws Exception {
        long antes = chatServicio.listarChat().size();
        Chat creado = chatServicio.obtenerOCrearChat(8L, 1L);
        sincronizar();
        assertNotNull(creado.getId());
        Chat otra = chatServicio.obtenerOCrearChat(8L, 1L);
        sincronizar();
        assertEquals(creado.getId(), otra.getId());
        assertEquals(antes + 1, chatServicio.listarChat().size());
    }

    @Test
    public void obtenerOCrearChatExistenteTest() throws Exception {
        assertEquals(1L, chatServicio.obtenerOCrearChat(5L, 3L).getId());
    }

    @Test
    public void obtenerOCrearChatProductoPropioTest() {
        // el usuario 5 es el vendedor del producto 1
        assertThrows(NegocioException.class, () -> chatServicio.obtenerOCrearChat(5L, 1L));
    }

    @Test
    public void obtenerOCrearChatProductoVencidoTest() {
        // el producto 6 está vencido y el usuario 5 no tiene chat sobre él
        assertThrows(NegocioException.class, () -> chatServicio.obtenerOCrearChat(5L, 6L));
    }

    @Test
    public void obtenerOCrearChatInexistentesTest() {
        assertThrows(NegocioException.class, () -> chatServicio.obtenerOCrearChat(9999L, 1L));
        assertThrows(NegocioException.class, () -> chatServicio.obtenerOCrearChat(8L, 9999L));
        assertThrows(NegocioException.class, () -> chatServicio.obtenerOCrearChat(null, 1L));
        assertThrows(NegocioException.class, () -> chatServicio.obtenerOCrearChat(8L, null));
    }

    @Test
    public void listarChatsUsuarioTest() throws Exception {
        assertEquals(2, chatServicio.listarChatsUsuario(5L).size());
        assertEquals(2, chatServicio.listarChatsUsuario(7L).size());
        assertThrows(NegocioException.class, () -> chatServicio.listarChatsUsuario(9999L));
    }

    @Test
    public void esParticipanteTest() {
        assertTrue(chatServicio.esParticipante(1L, 5L));
        assertTrue(chatServicio.esParticipante(1L, 7L));
        assertFalse(chatServicio.esParticipante(1L, 8L));
        assertFalse(chatServicio.esParticipante(null, 5L));
        assertFalse(chatServicio.esParticipante(1L, null));
        assertFalse(chatServicio.esParticipante(9999L, 5L));
    }

    @Test
    public void obtenerChatNuloTest() {
        assertThrows(NegocioException.class, () -> chatServicio.obtenerChat(null));
    }
}
