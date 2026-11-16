package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ComentarioServicioTest extends BaseServicioTest {

    @Autowired
    private ComentarioServicio comentarioServicio;
    @Autowired
    private UsuarioServicio usuarioServicio;
    @Autowired
    private ProductoServicio productoServicio;

    private Comentario nuevo(String mensaje, int calificacion, Long idUsuario, Long idProducto) {
        Comentario c = new Comentario();
        c.setMensaje(mensaje);
        c.setCalificacion(calificacion);
        if (idUsuario != null) {
            Usuario u = new Usuario();
            u.setId(idUsuario);
            c.setUsuario(u);
        }
        if (idProducto != null) {
            Producto p = new Producto();
            p.setId(idProducto);
            c.setProducto(p);
        }
        return c;
    }

    private String mensajeDe(Comentario c) {
        return assertThrows(NegocioException.class, () -> comentarioServicio.comentarProducto(c)).getMessage();
    }

    @Test
    public void registrarComentarioTest() throws Exception {
        Comentario c = new Comentario();
        c.setMensaje("Muy buen producto");
        c.setCalificacion(5);
        c.setFecha(new Date());
        c.setUsuario(usuarioServicio.obtenerUsuario("pedro@email.com"));
        c.setProducto(productoServicio.obtenerProducto(1L));
        Comentario guardado = comentarioServicio.registrarComentario(c);
        sincronizar();
        assertNotNull(guardado.getId());
        assertEquals("Muy buen producto", comentarioServicio.obtenerComentario(guardado.getId()).getMensaje());
    }

    @Test
    public void obtenerComentarioInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> comentarioServicio.obtenerComentario(99999L));
        assertEquals("El comentario no existe", e.getMessage());
    }

    @Test
    public void obtenerComentarioNuloTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> comentarioServicio.obtenerComentario(null));
        assertEquals("El comentario no existe", e.getMessage());
    }

    @Test
    public void comentarProductoTest() throws Exception {
        Comentario c = nuevo("  Excelente licuadora  ", 5, 6L, 4L);
        c.setId(777L);
        c.setRespuesta("no debe quedar");
        Comentario guardado = comentarioServicio.comentarProducto(c);
        sincronizar();
        Comentario leido = comentarioServicio.obtenerComentario(guardado.getId());
        assertNotEquals(777L, leido.getId());
        assertEquals("  Excelente licuadora  ", leido.getMensaje());
        assertEquals(5, leido.getCalificacion());
        assertNull(leido.getRespuesta());
        assertNotNull(leido.getFecha());
        assertEquals(6L, leido.getUsuario().getId());
        assertEquals(4L, leido.getProducto().getId());
    }

    @Test
    public void comentarVariasVecesPermitidoTest() throws Exception {
        comentarioServicio.comentarProducto(nuevo("Primero", 3, 6L, 4L));
        comentarioServicio.comentarProducto(nuevo("Segundo", 5, 6L, 4L));
        sincronizar();
        assertEquals(2, comentarioServicio.listarComentarioProducto(4L).size());
    }

    @Test
    public void comentarSinUsuarioTest() {
        assertEquals("El usuario es obligatorio", mensajeDe(nuevo("Hola", 3, null, 4L)));
        Comentario c = nuevo("Hola", 3, null, 4L);
        c.setUsuario(new Usuario());
        assertEquals("El usuario es obligatorio", mensajeDe(c));
    }

    @Test
    public void comentarSinProductoTest() {
        assertEquals("El producto es obligatorio", mensajeDe(nuevo("Hola", 3, 6L, null)));
    }

    @Test
    public void comentarUsuarioInexistenteTest() {
        assertEquals("El usuario no existe", mensajeDe(nuevo("Hola", 3, 99999L, 4L)));
    }

    @Test
    public void comentarProductoInexistenteTest() {
        assertEquals("El producto no existe", mensajeDe(nuevo("Hola", 3, 6L, 99999L)));
    }

    @Test
    public void comentarMensajeVacioTest() {
        assertEquals("El mensaje es obligatorio", mensajeDe(nuevo("   ", 3, 6L, 4L)));
        assertEquals("El mensaje es obligatorio", mensajeDe(nuevo(null, 3, 6L, 4L)));
    }

    @Test
    public void comentarMensajeLargoTest() throws Exception {
        assertEquals("El mensaje no puede superar los 1000 caracteres", mensajeDe(nuevo("a".repeat(1001), 3, 6L, 4L)));
        assertNotNull(comentarioServicio.comentarProducto(nuevo("a".repeat(1000), 3, 6L, 4L)).getId());
    }

    @Test
    public void comentarCalificacionInvalidaTest() throws Exception {
        assertEquals("La calificación debe estar entre 0 y 5", mensajeDe(nuevo("Hola", -1, 6L, 4L)));
        assertEquals("La calificación debe estar entre 0 y 5", mensajeDe(nuevo("Hola", 6, 6L, 4L)));
        assertNotNull(comentarioServicio.comentarProducto(nuevo("Hola", 0, 6L, 4L)).getId());
    }

    @Test
    public void comentarProductoPropioTest() {
        assertEquals("No puede comentar su propio producto", mensajeDe(nuevo("Hola", 3, 5L, 4L)));
    }

    @Test
    public void responderComentarioTest() throws Exception {
        // comentario 1: producto 1, vendedor 5
        comentarioServicio.responderComentario(1L, "  Gracias por tu compra  ", 5L);
        sincronizar();
        assertEquals("Gracias por tu compra", comentarioServicio.obtenerComentario(1L).getRespuesta());
    }

    @Test
    public void responderComentarioOtroUsuarioTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> comentarioServicio.responderComentario(1L, "Hola", 6L));
        assertEquals("Solo el vendedor del producto puede responder el comentario", e.getMessage());
        e = assertThrows(NegocioException.class, () -> comentarioServicio.responderComentario(1L, "Hola", null));
        assertEquals("Solo el vendedor del producto puede responder el comentario", e.getMessage());
    }

    @Test
    public void responderComentarioRespuestaVaciaTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> comentarioServicio.responderComentario(1L, "  ", 5L));
        assertEquals("La respuesta es obligatoria", e.getMessage());
        e = assertThrows(NegocioException.class, () -> comentarioServicio.responderComentario(1L, null, 5L));
        assertEquals("La respuesta es obligatoria", e.getMessage());
    }

    @Test
    public void responderComentarioRespuestaLargaTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> comentarioServicio.responderComentario(1L, "a".repeat(256), 5L));
        assertEquals("La respuesta no puede superar los 255 caracteres", e.getMessage());
    }

    @Test
    public void responderComentarioDosVecesTest() throws Exception {
        comentarioServicio.responderComentario(1L, "Primera", 5L);
        sincronizar();
        NegocioException e = assertThrows(NegocioException.class,
                () -> comentarioServicio.responderComentario(1L, "Segunda", 5L));
        assertEquals("El comentario ya tiene respuesta", e.getMessage());
        // el comentario 2 ya traía respuesta en los datos
        e = assertThrows(NegocioException.class, () -> comentarioServicio.responderComentario(2L, "Otra", 5L));
        assertEquals("El comentario ya tiene respuesta", e.getMessage());
    }

    @Test
    public void responderComentarioInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> comentarioServicio.responderComentario(99999L, "Hola", 5L));
        assertEquals("El comentario no existe", e.getMessage());
    }

    @Test
    public void calcularCalificacionPromedioTest() throws Exception {
        // producto 4 (vendedor 5) sin comentarios: 5, 4 y 3 de tres usuarios distintos
        comentarioServicio.comentarProducto(nuevo("Excelente", 5, 6L, 4L));
        comentarioServicio.comentarProducto(nuevo("Bueno", 4, 7L, 4L));
        comentarioServicio.comentarProducto(nuevo("Regular", 3, 8L, 4L));
        sincronizar();
        assertEquals(4.0, comentarioServicio.calcularCalificacion(4L), 0.0001);
    }

    @Test
    public void calcularCalificacionSinComentariosTest() throws Exception {
        assertEquals(0.0, comentarioServicio.calcularCalificacion(4L), 0.0001);
    }

    @Test
    public void calcularCalificacionRedondeoTest() throws Exception {
        // 5, 5 y 4 = 4.666... -> 4.7
        comentarioServicio.comentarProducto(nuevo("a", 5, 6L, 4L));
        comentarioServicio.comentarProducto(nuevo("b", 5, 7L, 4L));
        comentarioServicio.comentarProducto(nuevo("c", 4, 8L, 4L));
        sincronizar();
        assertEquals(4.7, comentarioServicio.calcularCalificacion(4L), 0.0001);
        // producto 1 con datos del SQL: 5 y 4 = 4.5
        assertEquals(4.5, comentarioServicio.calcularCalificacion(1L), 0.0001);
    }

    @Test
    public void calcularCalificacionProductoInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> comentarioServicio.calcularCalificacion(99999L));
        assertEquals("El producto no existe", e.getMessage());
        e = assertThrows(NegocioException.class, () -> comentarioServicio.calcularCalificacion(null));
        assertEquals("El producto no existe", e.getMessage());
    }
}
