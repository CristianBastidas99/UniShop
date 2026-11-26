package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.AdministradorServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class AdministradorServicioTest extends BaseServicioTest {

    @Autowired
    private AdministradorServicio administradorServicio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Administrador nuevoAdministrador(String email) {
        Administrador a = new Administrador();
        a.setNombre("Admin Nuevo");
        a.setEmail(email);
        a.setPassword("12345");
        return a;
    }

    @Test
    public void iniciarSesionCorrectoTest() throws Exception {
        Administrador a = administradorServicio.iniciarSesion("juanito@uniquindio.com", "12345");
        assertEquals(2L, a.getId());
        assertEquals("Juanito Grandez", a.getNombre());
    }

    @Test
    public void iniciarSesionEmailConMayusculasYEspaciosTest() throws Exception {
        Administrador a = administradorServicio.iniciarSesion("  JUANITO@Uniquindio.COM ", "12345");
        assertEquals("juanito@uniquindio.com", a.getEmail());
    }

    @Test
    public void iniciarSesionPasswordErradaTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> administradorServicio.iniciarSesion("juanito@uniquindio.com", "incorrecta"));
        assertEquals("Correo o contraseña incorrectos", e.getMessage());
    }

    @Test
    public void iniciarSesionEmailInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> administradorServicio.iniciarSesion("noexiste@uniquindio.com", "12345"));
        assertEquals("Correo o contraseña incorrectos", e.getMessage());
    }

    @Test
    public void iniciarSesionCamposVaciosTest() {
        String mensaje = "El correo y la contraseña son obligatorios";
        assertEquals(mensaje, assertThrows(NegocioException.class, () -> administradorServicio.iniciarSesion(null, "12345")).getMessage());
        assertEquals(mensaje, assertThrows(NegocioException.class, () -> administradorServicio.iniciarSesion("  ", "12345")).getMessage());
        assertEquals(mensaje, assertThrows(NegocioException.class, () -> administradorServicio.iniciarSesion("juanito@uniquindio.com", null)).getMessage());
        assertEquals(mensaje, assertThrows(NegocioException.class, () -> administradorServicio.iniciarSesion("juanito@uniquindio.com", "")).getMessage());
    }

    @Test
    public void registrarAdministradorTest() throws Exception {
        Administrador guardado = administradorServicio.registrarAdministrador(nuevoAdministrador("nuevo@admin.com"));
        sincronizar();
        assertNotNull(guardado.getId());
        assertEquals("nuevo@admin.com", administradorServicio.obtenerAdministrador("nuevo@admin.com").getEmail());
    }

    @Test
    public void registrarEmailDeAdministradorTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> administradorServicio.registrarAdministrador(nuevoAdministrador("juanito@uniquindio.com")));
        assertEquals("El correo juanito@uniquindio.com ya se encuentra registrado", e.getMessage());
    }

    @Test
    public void registrarEmailDeUsuarioTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> administradorServicio.registrarAdministrador(nuevoAdministrador("lorena@email.com")));
        assertEquals("El correo lorena@email.com ya se encuentra registrado", e.getMessage());
    }

    @Test
    public void obtenerAdministradorInexistenteTest() {
        assertThrows(NegocioException.class, () -> administradorServicio.obtenerAdministrador("noexiste@admin.com"));
    }

    @Test
    public void registrarEmailEnMayusculasSeNormalizaTest() throws Exception {
        administradorServicio.registrarAdministrador(nuevoAdministrador("  Nuevo@Admin.COM "));
        sincronizar();
        assertEquals("nuevo@admin.com", administradorServicio.obtenerAdministrador("nuevo@admin.com").getEmail());
    }

    @Test
    public void registrarGuardaLaPasswordEncriptadaTest() throws Exception {
        administradorServicio.registrarAdministrador(nuevoAdministrador("nuevo@admin.com"));
        sincronizar();
        String guardada = administradorServicio.obtenerAdministrador("nuevo@admin.com").getPassword();
        assertTrue(guardada.startsWith("$2a$"));
        assertTrue(passwordEncoder.matches("12345", guardada));
        assertEquals("nuevo@admin.com", administradorServicio.iniciarSesion("nuevo@admin.com", "12345").getEmail());
        assertThrows(NegocioException.class, () -> administradorServicio.iniciarSesion("nuevo@admin.com", guardada));
    }

    @Test
    public void actualizarSinPasswordConservaLaActualTest() throws Exception {
        Administrador datos = new Administrador();
        datos.setNombre("Juanito Cambiado");
        datos.setEmail("juanito@uniquindio.com");
        datos.setPassword(null);
        administradorServicio.actualizarAdministrador(datos);
        sincronizar();
        Administrador leido = administradorServicio.obtenerAdministrador("juanito@uniquindio.com");
        assertEquals("Juanito Cambiado", leido.getNombre());
        assertEquals(2L, leido.getId());
        assertEquals(2L, administradorServicio.iniciarSesion("juanito@uniquindio.com", "12345").getId());
    }

    @Test
    public void actualizarConPasswordLaEncriptaTest() throws Exception {
        Administrador datos = new Administrador();
        datos.setNombre("Juanito Grandez");
        datos.setEmail("juanito@uniquindio.com");
        datos.setPassword("claveNueva9");
        administradorServicio.actualizarAdministrador(datos);
        sincronizar();
        String guardada = administradorServicio.obtenerAdministrador("juanito@uniquindio.com").getPassword();
        assertNotEquals("claveNueva9", guardada);
        assertTrue(passwordEncoder.matches("claveNueva9", guardada));
        assertThrows(NegocioException.class, () -> administradorServicio.iniciarSesion("juanito@uniquindio.com", "12345"));
    }

    @Test
    public void cambiarPasswordEncriptaYValidaLongitudTest() throws Exception {
        administradorServicio.cambiarPassword("juanito@uniquindio.com", "otraClave1");
        sincronizar();
        String guardada = administradorServicio.obtenerAdministrador("juanito@uniquindio.com").getPassword();
        assertTrue(passwordEncoder.matches("otraClave1", guardada));
        assertEquals(2L, administradorServicio.iniciarSesion("juanito@uniquindio.com", "otraClave1").getId());
        assertThrows(NegocioException.class,
                () -> administradorServicio.cambiarPassword("juanito@uniquindio.com", "a".repeat(73)));
        Administrador largo = nuevoAdministrador("largo@admin.com");
        largo.setPassword("a".repeat(73));
        assertThrows(NegocioException.class, () -> administradorServicio.registrarAdministrador(largo));
    }
}
