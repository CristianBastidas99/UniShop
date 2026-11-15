package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class ComentarioTest extends CrudTestBase {

    @Autowired
    private ComentarioRepo comentarioRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Test
    public void registrarComentarioTest() {
        Comentario c = new Comentario();
        c.setMensaje("Excelente licuadora, muy potente");
        c.setCalificacion(5);
        c.setFecha(new Date());
        c.setUsuario(usuarioRepo.findById(6L).orElseThrow());
        c.setProducto(productoRepo.findById(4L).orElseThrow());

        Comentario guardado = comentarioRepo.saveAndFlush(c);
        sincronizar();

        Comentario buscado = comentarioRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals(5, buscado.getCalificacion());
        Assertions.assertEquals("Licuadora", buscado.getProducto().getNombre());
        Assertions.assertNull(buscado.getRespuesta());
    }

    @Test
    public void eliminarComentarioTest() {
        comentarioRepo.deleteById(1L);
        sincronizar();

        Assertions.assertTrue(comentarioRepo.findById(1L).isEmpty());
        Assertions.assertEquals(4, comentarioRepo.count());
    }

    @Test
    public void actualizarComentarioTest() {
        // El vendedor responde el comentario 3
        Comentario c = comentarioRepo.findById(3L).orElseThrow();
        c.setRespuesta("Gracias, en la proxima version mejoramos la camara");
        comentarioRepo.saveAndFlush(c);
        sincronizar();

        Assertions.assertEquals("Gracias, en la proxima version mejoramos la camara",
                comentarioRepo.findById(3L).orElseThrow().getRespuesta());
    }

    @Test
    public void listarComentarioTest() {
        List<Comentario> lista = comentarioRepo.findAll();
        Assertions.assertEquals(5, lista.size());
        Assertions.assertEquals(2, comentarioRepo.listarPorProducto(1L).size());
    }
}
