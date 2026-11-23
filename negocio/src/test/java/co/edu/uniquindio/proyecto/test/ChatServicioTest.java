package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.chat.ChatResumenDTO;
import co.edu.uniquindio.proyecto.dto.chat.RolChat;
import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ChatServicio;
import co.edu.uniquindio.proyecto.servicios.MensajeServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServicioTest extends BaseServicioTest {

    @Autowired
    private ChatServicio chatServicio;
    @Autowired
    private MensajeServicio mensajeServicio;
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
    public void resumenChatsComoCompradorYVendedorTest() throws Exception {
        // lorena (5): compradora en el chat 1 (producto 3) y vendedora en el chat 3 (producto 1)
        List<ChatResumenDTO> resumen = chatServicio.listarResumenChats(5L);
        assertEquals(2, resumen.size());

        ChatResumenDTO comoComprador = resumen.get(0);
        assertEquals(1L, comoComprador.getIdChat());
        assertEquals(RolChat.COMPRADOR, comoComprador.getRol());
        assertEquals(7L, comoComprador.getIdOtraPersona());
        assertEquals("Pepe Perez", comoComprador.getNombreOtraPersona());
        assertEquals(3L, comoComprador.getIdProducto());
        assertEquals("Juguete Perro", comoComprador.getNombreProducto());
        assertEquals("juguete-perro.png", comoComprador.getImagenProducto());
        assertEquals("Si, quedan 50 unidades", comoComprador.getUltimoMensaje());
        assertNotNull(comoComprador.getFechaUltimoMensaje());
        assertFalse(comoComprador.isUltimoMensajePropio());

        ChatResumenDTO comoVendedor = resumen.get(1);
        assertEquals(3L, comoVendedor.getIdChat());
        assertEquals(RolChat.VENDEDOR, comoVendedor.getRol());
        assertEquals("Marta Wayne", comoVendedor.getNombreOtraPersona());
        assertEquals("Cepillo Electrico", comoVendedor.getNombreProducto());
        assertEquals("Hace envios a Cartagena?", comoVendedor.getUltimoMensaje());
        assertFalse(comoVendedor.isUltimoMensajePropio());
    }

    @Test
    public void resumenChatsMensajePropioTest() throws Exception {
        // pepe (7): vendedor en el chat 1 y comprador en el chat 2; en ambos escribió él el último mensaje
        List<ChatResumenDTO> resumen = chatServicio.listarResumenChats(7L);
        assertEquals(2, resumen.size());
        assertEquals(1L, resumen.get(0).getIdChat());
        assertEquals(RolChat.VENDEDOR, resumen.get(0).getRol());
        assertEquals("Lorena Lopez", resumen.get(0).getNombreOtraPersona());
        assertTrue(resumen.get(0).isUltimoMensajePropio());
        assertEquals(2L, resumen.get(1).getIdChat());
        assertEquals(RolChat.COMPRADOR, resumen.get(1).getRol());
        assertEquals("Marta Wayne", resumen.get(1).getNombreOtraPersona());
        assertTrue(resumen.get(1).isUltimoMensajePropio());
    }

    @Test
    public void resumenChatsOrdenPorActividadTest() throws Exception {
        // un mensaje nuevo en el chat 3 lo pasa al primer lugar de lorena (5)
        mensajeServicio.enviarMensaje(3L, 5L, "Si, hacemos envios");
        sincronizar();
        List<ChatResumenDTO> resumen = chatServicio.listarResumenChats(5L);
        assertEquals(3L, resumen.get(0).getIdChat());
        assertEquals("Si, hacemos envios", resumen.get(0).getUltimoMensaje());
        assertTrue(resumen.get(0).isUltimoMensajePropio());
        assertEquals(1L, resumen.get(1).getIdChat());
    }

    @Test
    public void resumenChatSinMensajesAlFinalTest() throws Exception {
        // pedro (8) es el vendedor del chat 4, que no tiene mensajes
        List<ChatResumenDTO> soloVacio = chatServicio.listarResumenChats(8L);
        assertEquals(1, soloVacio.size());
        assertEquals(RolChat.VENDEDOR, soloVacio.get(0).getRol());
        assertEquals("Sofia Ramirez", soloVacio.get(0).getNombreOtraPersona());
        assertEquals("portatil.png", soloVacio.get(0).getImagenProducto());
        assertNull(soloVacio.get(0).getUltimoMensaje());
        assertNull(soloVacio.get(0).getFechaUltimoMensaje());
        assertFalse(soloVacio.get(0).isUltimoMensajePropio());

        // marta (6) abre un chat nuevo sobre el producto 5: sin mensajes, va después de los que sí tienen
        Chat nuevo = chatServicio.obtenerOCrearChat(6L, 5L);
        sincronizar();
        List<ChatResumenDTO> resumen = chatServicio.listarResumenChats(6L);
        assertEquals(3, resumen.size());
        assertEquals(nuevo.getId(), resumen.get(2).getIdChat());
        assertNull(resumen.get(2).getUltimoMensaje());
        assertNotNull(resumen.get(0).getFechaUltimoMensaje());
        assertNotNull(resumen.get(1).getFechaUltimoMensaje());
    }

    @Test
    public void resumenChatsMensajeLargoSeRecortaTest() throws Exception {
        mensajeServicio.enviarMensaje(1L, 5L, "a".repeat(300));
        sincronizar();
        ChatResumenDTO primero = chatServicio.listarResumenChats(5L).get(0);
        assertEquals(1L, primero.getIdChat());
        assertEquals(80, primero.getUltimoMensaje().length());
        assertTrue(primero.getUltimoMensaje().endsWith("…"));
        assertTrue(primero.isUltimoMensajePropio());
    }

    @Test
    public void resumenChatsUsuarioInexistenteTest() {
        assertThrows(NegocioException.class, () -> chatServicio.listarResumenChats(9999L));
        assertThrows(NegocioException.class, () -> chatServicio.listarResumenChats(null));
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
