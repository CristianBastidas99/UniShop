package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.servicios.AdministradorServicio;
import co.edu.uniquindio.proyecto.servicios.MigracionPasswordServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

/** Migración de contraseñas en texto plano (de versiones anteriores) a BCrypt. */
public class MigracionPasswordServicioTest extends BaseServicioTest {

    @Autowired
    private MigracionPasswordServicio migracionServicio;
    @Autowired
    private UsuarioServicio usuarioServicio;
    @Autowired
    private AdministradorServicio administradorServicio;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void sinPendientesNoCambiaNadaTest() {
        // unishop.sql ya trae las contraseñas con BCrypt
        assertEquals(0, migracionServicio.encriptarPasswordsPendientes());
    }

    @Test
    public void encriptaSoloLasQueEstanEnTextoPlanoTest() throws Exception {
        jdbc.update("UPDATE usuario SET password = 'claveVieja1' WHERE id = 5");
        jdbc.update("UPDATE usuario SET password = 'otraVieja2' WHERE id = 6");
        jdbc.update("UPDATE administrador SET password = 'adminVieja3' WHERE id = 1");
        // Un texto que empieza con $2a$ pero no es un hash completo también se considera texto plano
        jdbc.update("UPDATE administrador SET password = '$2a$no-es-un-hash' WHERE id = 2");

        assertEquals(4, migracionServicio.encriptarPasswordsPendientes());
        sincronizar();

        assertTrue(usuarioServicio.obtenerUsuario("lorena@email.com").getPassword().startsWith("$2a$10$"));
        assertEquals(5L, usuarioServicio.iniciarSesion("lorena@email.com", "claveVieja1").getId());
        assertEquals(6L, usuarioServicio.iniciarSesion("marta@email.com", "otraVieja2").getId());
        assertEquals(1L, administradorServicio.iniciarSesion("cfbastidaso@uqvirtual.edu.co", "adminVieja3").getId());
        assertEquals(2L, administradorServicio.iniciarSesion("juanito@uniquindio.com", "$2a$no-es-un-hash").getId());
        // Las que ya estaban encriptadas siguen funcionando
        assertEquals(7L, usuarioServicio.iniciarSesion("pepe@email.com", "12345").getId());

        // Es idempotente: una segunda ejecución no vuelve a encriptar nada
        assertEquals(0, migracionServicio.encriptarPasswordsPendientes());
    }
}
