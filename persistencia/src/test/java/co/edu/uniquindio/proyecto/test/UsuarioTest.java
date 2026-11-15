package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.CiudadRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class UsuarioTest extends CrudTestBase {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private CiudadRepo ciudadRepo;

    @Autowired
    private ProductoRepo productoRepo;

    private Usuario nuevoUsuario(String email) {
        Usuario u = new Usuario();
        u.setNombre("Juanita Gomez");
        u.setEmail(email);
        u.setPassword("12345");
        u.setCiudad(ciudadRepo.findById(1L).orElseThrow());
        u.setNumeroTelefono(new ArrayList<>(List.of("3001112233", "3104445566")));
        return u;
    }

    @Test
    public void registrarUsuarioTest() {
        Usuario guardado = usuarioRepo.saveAndFlush(nuevoUsuario("juanita@email.com"));
        sincronizar();

        Usuario buscado = usuarioRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals("juanita@email.com", buscado.getEmail());
        Assertions.assertEquals("Armenia", buscado.getCiudad().getNombre());
        Assertions.assertEquals(List.of("3001112233", "3104445566"), buscado.getNumeroTelefono());
    }

    @Test
    public void eliminarUsuarioTest() {
        // Los usuarios del SQL tienen productos, compras o comentarios, así que se elimina uno nuevo
        Usuario guardado = usuarioRepo.saveAndFlush(nuevoUsuario("borrar@email.com"));
        usuarioRepo.delete(guardado);
        sincronizar();

        Assertions.assertTrue(usuarioRepo.findById(guardado.getId()).isEmpty());
        Assertions.assertEquals(5, usuarioRepo.count());
    }

    @Test
    public void actualizarUsuarioTest() {
        Usuario u = usuarioRepo.findById(5L).orElseThrow();
        u.setNombre("Lorena Lopez Gil");
        u.getNumeroTelefono().add("3209998877");
        u.getMisProductosFavoritos().add(productoRepo.findById(3L).orElseThrow());
        usuarioRepo.saveAndFlush(u);
        sincronizar();

        Usuario buscado = usuarioRepo.findById(5L).orElseThrow();
        Assertions.assertEquals("Lorena Lopez Gil", buscado.getNombre());
        Assertions.assertEquals(3, buscado.getNumeroTelefono().size());
        Assertions.assertEquals(3, buscado.getMisProductosFavoritos().size());
    }

    @Test
    public void listarUsuariosTest() {
        List<Usuario> lista = usuarioRepo.findAll();
        Assertions.assertEquals(5, lista.size());
        Assertions.assertEquals("Lorena Lopez", usuarioRepo.findByEmail("lorena@email.com").orElseThrow().getNombre());
    }
}
