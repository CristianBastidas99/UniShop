package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioServicioTest extends BaseServicioTest {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    // ---------- Fase 5: activar/desactivar, actividad, eliminación, búsqueda, actualización ----------

    @Test
    public void iniciarSesionUsuarioInactivoTest() throws Exception {
        usuarioServicio.cambiarActivo("lorena@email.com", false);
        sincronizar();
        NegocioException e = assertThrows(NegocioException.class,
                () -> usuarioServicio.iniciarSesion("lorena@email.com", "12345"));
        assertEquals("Tu cuenta está desactivada. Comunícate con el administrador", e.getMessage());
        // Con contraseña errónea el mensaje sigue siendo el genérico.
        NegocioException e2 = assertThrows(NegocioException.class,
                () -> usuarioServicio.iniciarSesion("lorena@email.com", "mala"));
        assertEquals("Correo o contraseña incorrectos", e2.getMessage());
    }

    @Test
    public void usuariosDelSqlEstanActivosTest() throws Exception {
        assertTrue(usuarioServicio.obtenerUsuario("lorena@email.com").isActivo());
    }

    @Test
    public void cambiarActivoTest() throws Exception {
        usuarioServicio.cambiarActivo("lorena@email.com", false);
        sincronizar();
        assertFalse(usuarioServicio.obtenerUsuario("lorena@email.com").isActivo());
        usuarioServicio.cambiarActivo("lorena@email.com", true);
        sincronizar();
        assertTrue(usuarioServicio.obtenerUsuario("lorena@email.com").isActivo());
        assertEquals(5L, usuarioServicio.iniciarSesion("lorena@email.com", "12345").getId());
    }

    @Test
    public void cambiarActivoInexistenteTest() {
        assertThrows(NegocioException.class, () -> usuarioServicio.cambiarActivo("noexiste@mail.com", false));
    }

    @Test
    public void tieneActividadTest() throws Exception {
        assertTrue(usuarioServicio.tieneActividad("lorena@email.com"));
        usuarioServicio.registrarUsuario(nuevoUsuario("juana@mail.com"));
        sincronizar();
        assertFalse(usuarioServicio.tieneActividad("juana@mail.com"));
    }

    @Test
    public void tieneActividadInexistenteTest() {
        assertThrows(NegocioException.class, () -> usuarioServicio.tieneActividad("noexiste@mail.com"));
    }

    @Test
    public void eliminarUsuarioConActividadTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> usuarioServicio.eliminarUsuario("lorena@email.com"));
        assertEquals("El usuario lorena@email.com tiene actividad (productos, compras, comentarios...) "
                + "y no se puede eliminar; puedes desactivarlo", e.getMessage());
    }

    @Test
    public void eliminarUsuarioSinActividadTest() throws Exception {
        usuarioServicio.registrarUsuario(nuevoUsuario("juana@mail.com"));
        sincronizar();
        usuarioServicio.eliminarUsuario("juana@mail.com");
        sincronizar();
        assertThrows(NegocioException.class, () -> usuarioServicio.obtenerUsuario("juana@mail.com"));
    }

    @Test
    public void buscarUsuariosPorNombreParcialTest() {
        List<Usuario> lista = usuarioServicio.buscarUsuarios("per");
        assertEquals(1, lista.size());
        assertEquals("Pepe Perez", lista.get(0).getNombre());
    }

    @Test
    public void buscarUsuariosPorEmailEnMayusculasTest() {
        List<Usuario> lista = usuarioServicio.buscarUsuarios("MARTA@EMAIL");
        assertEquals(1, lista.size());
        assertEquals("marta@email.com", lista.get(0).getEmail());
    }

    @Test
    public void buscarUsuariosVacioDevuelveTodosOrdenadosTest() {
        for (String texto : new String[]{null, "", "   "}) {
            List<Usuario> lista = usuarioServicio.buscarUsuarios(texto);
            assertEquals(5, lista.size());
            assertEquals("Lorena Lopez", lista.get(0).getNombre());
            assertEquals("Sofia Ramirez", lista.get(4).getNombre());
        }
    }

    @Test
    public void buscarUsuariosSinCoincidenciasTest() {
        assertTrue(usuarioServicio.buscarUsuarios("zzzz").isEmpty());
    }

    private Usuario datosEdicion(String nombre, String password) {
        Usuario u = new Usuario();
        u.setEmail("lorena@email.com");
        u.setNombre(nombre);
        u.setPassword(password);
        u.setCiudad(ciudad(2L));
        u.setNumeroTelefono(new ArrayList<>(Arrays.asList("3000000001")));
        return u;
    }

    @Test
    public void actualizarSinPasswordConservaLaActualTest() throws Exception {
        usuarioServicio.actualizarUsuario(datosEdicion("Lorena Nueva", null));
        usuarioServicio.actualizarUsuario(datosEdicion("Lorena Nueva", "   "));
        sincronizar();
        Usuario leido = usuarioServicio.obtenerUsuario("lorena@email.com");
        assertTrue(passwordEncoder.matches("12345", leido.getPassword()));
        assertEquals("Lorena Nueva", leido.getNombre());
        assertEquals(2L, leido.getCiudad().getId());
        assertEquals(Arrays.asList("3000000001"), leido.getNumeroTelefono());
        assertTrue(leido.isActivo());
        assertEquals(5L, leido.getId());
    }

    @Test
    public void actualizarConPasswordLaCambiaTest() throws Exception {
        usuarioServicio.actualizarUsuario(datosEdicion("Lorena Lopez", "nueva123"));
        sincronizar();
        String guardada = usuarioServicio.obtenerUsuario("lorena@email.com").getPassword();
        assertNotEquals("nueva123", guardada);
        assertTrue(passwordEncoder.matches("nueva123", guardada));
        assertEquals(5L, usuarioServicio.iniciarSesion("lorena@email.com", "nueva123").getId());
    }

    @Test
    public void actualizarNoPierdeActivoNiRelacionesTest() throws Exception {
        usuarioServicio.cambiarActivo("lorena@email.com", false);
        sincronizar();
        usuarioServicio.actualizarUsuario(datosEdicion("Lorena Lopez", null));
        sincronizar();
        assertFalse(usuarioServicio.obtenerUsuario("lorena@email.com").isActivo());
        assertTrue(usuarioServicio.tieneActividad("lorena@email.com"));
    }

    @Test
    public void registrarGuardaLaPasswordEncriptadaTest() throws Exception {
        usuarioServicio.registrarUsuario(nuevoUsuario("juana@mail.com"));
        sincronizar();
        String guardada = usuarioServicio.obtenerUsuario("juana@mail.com").getPassword();
        assertNotEquals("12345", guardada);
        assertTrue(guardada.startsWith("$2a$"));
        assertEquals("juana@mail.com", usuarioServicio.iniciarSesion("juana@mail.com", "12345").getEmail());
        assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion("juana@mail.com", guardada));
    }

    @Test
    public void registrarSinPasswordTest() {
        Usuario u = nuevoUsuario("juana@mail.com");
        u.setPassword("  ");
        assertRegistroFalla(u, "La contraseña es obligatoria");
    }

    @Test
    public void passwordDeMasDe72BytesSeRechazaTest() throws Exception {
        String larga = "a".repeat(73);
        String mensaje = "La contraseña es demasiado larga: no puede superar 72 bytes "
                + "(con tildes o símbolos especiales ocupa menos caracteres)";
        Usuario u = nuevoUsuario("juana@mail.com");
        u.setPassword(larga);
        assertRegistroFalla(u, mensaje);
        // 40 letras "ñ" son 40 caracteres pero 80 bytes en UTF-8
        u.setPassword("ñ".repeat(40));
        assertRegistroFalla(u, mensaje);
        assertEquals(mensaje, assertThrows(NegocioException.class,
                () -> usuarioServicio.actualizarUsuario(datosEdicion("Lorena", larga))).getMessage());
        assertEquals(mensaje, assertThrows(NegocioException.class,
                () -> usuarioServicio.cambiarPassword("lorena@email.com", larga)).getMessage());
        // 72 bytes exactos sí se aceptan
        usuarioServicio.cambiarPassword("lorena@email.com", "a".repeat(72));
        assertNotNull(usuarioServicio.iniciarSesion("lorena@email.com", "a".repeat(72)));
    }

    @Test
    public void cambiarPasswordEncriptaTest() throws Exception {
        usuarioServicio.cambiarPassword("lorena@email.com", "otraClave1");
        sincronizar();
        String guardada = usuarioServicio.obtenerUsuario("lorena@email.com").getPassword();
        assertTrue(passwordEncoder.matches("otraClave1", guardada));
        assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion("lorena@email.com", "12345"));
        assertEquals(5L, usuarioServicio.iniciarSesion("lorena@email.com", "otraClave1").getId());
    }

    @Test
    public void loginConHashEnTextoPlanoNoFuncionaTest() throws Exception {
        // El hash guardado no sirve como contraseña
        String hash = usuarioServicio.obtenerUsuario("lorena@email.com").getPassword();
        assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion("lorena@email.com", hash));
    }

    @Test
    public void estaActivoTest() throws Exception {
        assertTrue(usuarioServicio.estaActivo(5L));
        usuarioServicio.cambiarActivo("lorena@email.com", false);
        sincronizar();
        assertFalse(usuarioServicio.estaActivo(5L));
        usuarioServicio.cambiarActivo("lorena@email.com", true);
        sincronizar();
        assertTrue(usuarioServicio.estaActivo(5L));
        assertFalse(usuarioServicio.estaActivo(null));
        assertFalse(usuarioServicio.estaActivo(999999L));
        // Un administrador no es un usuario
        assertFalse(usuarioServicio.estaActivo(1L));
    }

    @Test
    public void usuarioDesactivadoConPasswordIncorrectaRecibeMensajeGenericoTest() throws Exception {
        usuarioServicio.cambiarActivo("lorena@email.com", false);
        sincronizar();
        assertEquals("Correo o contraseña incorrectos", assertThrows(NegocioException.class,
                () -> usuarioServicio.iniciarSesion("lorena@email.com", "mala")).getMessage());
        assertEquals("Tu cuenta está desactivada. Comunícate con el administrador", assertThrows(NegocioException.class,
                () -> usuarioServicio.iniciarSesion("lorena@email.com", "12345")).getMessage());
    }
}
