package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioServicioTest extends BaseServicioTest {

    @Autowired
    private UsuarioServicio usuarioServicio;

    private Usuario nuevoUsuario(String email) {
        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail(email);
        u.setPassword("12345");
        u.setCiudad(ciudad(1L));
        u.setNumeroTelefono(new ArrayList<>(Arrays.asList("3001112233")));
        return u;
    }

    private Ciudad ciudad(Long id) {
        Ciudad c = new Ciudad();
        c.setId(id);
        return c;
    }

    private void assertRegistroFalla(Usuario u, String mensaje) {
        NegocioException e = assertThrows(NegocioException.class, () -> usuarioServicio.registrarUsuario(u));
        assertEquals(mensaje, e.getMessage());
    }

    @Test
    public void iniciarSesionCorrectoTest() throws Exception {
        Usuario u = usuarioServicio.iniciarSesion("lorena@email.com", "12345");
        assertEquals(5L, u.getId());
        assertEquals("Lorena Lopez", u.getNombre());
    }

    @Test
    public void iniciarSesionEmailConMayusculasYEspaciosTest() throws Exception {
        Usuario u = usuarioServicio.iniciarSesion("  LORENA@Email.COM ", "12345");
        assertEquals("lorena@email.com", u.getEmail());
    }

    @Test
    public void iniciarSesionPasswordErradaTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> usuarioServicio.iniciarSesion("lorena@email.com", "incorrecta"));
        assertEquals("Correo o contraseña incorrectos", e.getMessage());
    }

    @Test
    public void iniciarSesionEmailInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> usuarioServicio.iniciarSesion("noexiste@email.com", "12345"));
        assertEquals("Correo o contraseña incorrectos", e.getMessage());
    }

    @Test
    public void iniciarSesionCamposVaciosTest() {
        String mensaje = "El correo y la contraseña son obligatorios";
        assertEquals(mensaje, assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion(null, "12345")).getMessage());
        assertEquals(mensaje, assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion("  ", "12345")).getMessage());
        assertEquals(mensaje, assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion("lorena@email.com", null)).getMessage());
        assertEquals(mensaje, assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion("lorena@email.com", "")).getMessage());
    }

    @Test
    public void registrarConCiudadYTelefonosTest() throws Exception {
        Usuario u = nuevoUsuario("juana@mail.com");
        u.setCiudad(ciudad(3L));
        u.setNumeroTelefono(new ArrayList<>(Arrays.asList("3001112233", "+573004445566")));
        usuarioServicio.registrarUsuario(u);
        sincronizar();
        Usuario leido = usuarioServicio.obtenerUsuario("juana@mail.com");
        assertEquals(3L, leido.getCiudad().getId());
        assertEquals("Bogota", leido.getCiudad().getNombre());
        assertEquals(2, leido.getNumeroTelefono().size());
        assertTrue(leido.getNumeroTelefono().containsAll(Arrays.asList("3001112233", "+573004445566")));
    }

    @Test
    public void registrarSinCiudadTest() {
        Usuario u = nuevoUsuario("juana@mail.com");
        u.setCiudad(null);
        assertRegistroFalla(u, "La ciudad es obligatoria");
        u.setCiudad(new Ciudad());
        assertRegistroFalla(u, "La ciudad es obligatoria");
    }

    @Test
    public void registrarCiudadInexistenteTest() {
        Usuario u = nuevoUsuario("juana@mail.com");
        u.setCiudad(ciudad(999L));
        assertRegistroFalla(u, "La ciudad no existe");
    }

    @Test
    public void registrarSinTelefonosTest() {
        Usuario u = nuevoUsuario("juana@mail.com");
        u.setNumeroTelefono(null);
        assertRegistroFalla(u, "Debe registrar al menos un teléfono");
        u.setNumeroTelefono(new ArrayList<>());
        assertRegistroFalla(u, "Debe registrar al menos un teléfono");
    }

    @Test
    public void registrarTelefonoInvalidoTest() {
        Usuario u = nuevoUsuario("juana@mail.com");
        u.setNumeroTelefono(new ArrayList<>(Arrays.asList("300abc4567")));
        assertRegistroFalla(u, "El teléfono 300abc4567 no es válido");
        u.setNumeroTelefono(new ArrayList<>(Arrays.asList("123456")));
        assertRegistroFalla(u, "El teléfono 123456 no es válido");
    }

    @Test
    public void registrarTelefonosRepetidosTest() throws Exception {
        Usuario u = nuevoUsuario("juana@mail.com");
        u.setNumeroTelefono(new ArrayList<>(Arrays.asList("3001112233", " 3001112233 ", "3004445566")));
        usuarioServicio.registrarUsuario(u);
        sincronizar();
        List<String> telefonos = usuarioServicio.obtenerUsuario("juana@mail.com").getNumeroTelefono();
        assertEquals(2, telefonos.size());
        assertTrue(telefonos.containsAll(Arrays.asList("3001112233", "3004445566")));
    }

    @Test
    public void registrarEmailConMayusculasTest() throws Exception {
        usuarioServicio.registrarUsuario(nuevoUsuario("  Juana@MAIL.com "));
        sincronizar();
        assertEquals("juana@mail.com", usuarioServicio.obtenerUsuario("juana@mail.com").getEmail());
        assertRegistroFalla(nuevoUsuario("JUANA@mail.com"), "El correo juana@mail.com ya se encuentra registrado");
    }

    @Test
    public void actualizarSinTelefonosTest() throws Exception {
        Usuario u = usuarioServicio.obtenerUsuario("lorena@email.com");
        u.setNumeroTelefono(new ArrayList<>());
        NegocioException e = assertThrows(NegocioException.class, () -> usuarioServicio.actualizarUsuario(u));
        assertEquals("Debe registrar al menos un teléfono", e.getMessage());
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
