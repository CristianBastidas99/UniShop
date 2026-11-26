package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.servicios.MigracionPasswordServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, encripta con BCrypt las contraseñas que quedaron en texto plano de versiones anteriores.
 * Corre antes que {@link DatosInicialesConfig}, {@link AdministradorInicialConfig} y {@link DatosDemoConfig}.
 */
@Component
@Order(5)
public class MigracionPasswordsConfig implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(MigracionPasswordsConfig.class);

    private final MigracionPasswordServicio migracionServicio;

    public MigracionPasswordsConfig(MigracionPasswordServicio migracionServicio) {
        this.migracionServicio = migracionServicio;
    }

    @Override
    public void run(String... args) {
        try {
            int cambiadas = migracionServicio.encriptarPasswordsPendientes();
            if (cambiadas > 0) {
                LOG.info("Se encriptaron {} contraseñas que estaban en texto plano", cambiadas);
            }
        } catch (Exception e) {
            // La aplicación sigue arrancando; las contraseñas pendientes no permitirán iniciar sesión hasta migrarlas
            LOG.error("No se pudieron encriptar las contraseñas pendientes: {}", e.getMessage(), e);
        }
    }
}
