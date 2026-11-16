package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Comentario;
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
}
