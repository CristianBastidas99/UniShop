package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.servicios.AdministradorServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial al arrancar, solo si no hay administradores.
 * Los datos salen de application.properties (unishop.admin.*) o de las variables ADMIN_NOMBRE, ADMIN_EMAIL y ADMIN_PASSWORD.
 */
@Component
@Order(20)
public class AdministradorInicialConfig implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdministradorInicialConfig.class);

    private final AdministradorServicio administradorServicio;

    @Value("${unishop.admin.nombre}")
    private String nombre;

    @Value("${unishop.admin.email}")
    private String email;

    @Value("${unishop.admin.password}")
    private String password;

    public AdministradorInicialConfig(AdministradorServicio administradorServicio) {
        this.administradorServicio = administradorServicio;
    }

    @Override
    public void run(String... args) {
        try {
            if (!administradorServicio.listarAdministrador().isEmpty()) {
                return;
            }
            Administrador admin = new Administrador();
            admin.setNombre(nombre);
            admin.setEmail(email);
            admin.setPassword(password);
            administradorServicio.registrarAdministrador(admin);
            LOG.info("Se creó el administrador inicial con el correo {}", email);
        } catch (Exception e) {
            // La aplicación sigue arrancando aunque no se pueda crear (p. ej. el correo ya lo usa un usuario)
            LOG.warn("No se pudo crear el administrador inicial ({}): {}", email, e.getMessage());
        }
    }
}
