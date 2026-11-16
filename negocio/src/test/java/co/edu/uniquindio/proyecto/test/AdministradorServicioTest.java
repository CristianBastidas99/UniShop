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
}
