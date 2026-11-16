package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioServicioTest extends BaseServicioTest {

    @Autowired
    private UsuarioServicio usuarioServicio;

    private Usuario nuevoUsuario(String email) {
        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail(email);
        u.setPassword("12345");
        return u;
    }

    @Test
    public void registrarUsuarioTest() throws Exception {
        Usuario guardado = usuarioServicio.registrarUsuario(nuevoUsuario("juana@mail.com"));
        sincronizar();
        assertNotNull(guardado.getId());
        assertEquals("juana@mail.com", usuarioServicio.obtenerUsuario("juana@mail.com").getEmail());
    }

    @Test
    public void registrarEmailRepetidoDeUsuarioTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> usuarioServicio.registrarUsuario(nuevoUsuario("lorena@email.com")));
        assertEquals("El correo lorena@email.com ya se encuentra registrado", e.getMessage());
    }

    @Test
    public void registrarEmailDeAdministradorTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> usuarioServicio.registrarUsuario(nuevoUsuario("juanito@uniquindio.com")));
        assertEquals("El correo juanito@uniquindio.com ya se encuentra registrado", e.getMessage());
    }

    @Test
    public void actualizarUsuarioTest() throws Exception {
        Usuario u = usuarioServicio.obtenerUsuario("lorena@email.com");
        u.setNombre("Lorena Gomez");
        usuarioServicio.actualizarUsuario(u);
        sincronizar();
        assertEquals("Lorena Gomez", usuarioServicio.obtenerUsuario("lorena@email.com").getNombre());
    }

    @Test
    public void eliminarUsuarioTest() throws Exception {
        usuarioServicio.registrarUsuario(nuevoUsuario("juana@mail.com"));
        usuarioServicio.eliminarUsuario("juana@mail.com");
        sincronizar();
        assertThrows(NegocioException.class, () -> usuarioServicio.obtenerUsuario("juana@mail.com"));
    }

    @Test
    public void obtenerUsuarioInexistenteTest() {
        assertThrows(NegocioException.class, () -> usuarioServicio.obtenerUsuario("noexiste@mail.com"));
    }

    @Test
    public void listarUsuariosTest() {
        assertEquals(5, usuarioServicio.listarUsuarios().size());
    }
}
