package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.AdministradorServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class AdministradorServicioTest extends BaseServicioTest {

    @Autowired
    private AdministradorServicio administradorServicio;

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
}
