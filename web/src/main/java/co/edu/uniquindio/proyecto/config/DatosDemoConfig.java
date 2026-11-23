package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.dto.demo.ResumenCargaDemo;
import co.edu.uniquindio.proyecto.servicios.CargaDemoServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Carga los datos de demostración al arrancar, solo si {@code unishop.demo.cargar=true}
 * (variable de entorno DEMO_CARGAR). Corre después de {@link DatosInicialesConfig} y
 * {@link AdministradorInicialConfig}. No repite nada si los datos ya están cargados.
 * Cada compra de la carga envía su correo (a Mailpit en desarrollo).
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "unishop.demo.cargar", havingValue = "true")
public class DatosDemoConfig implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatosDemoConfig.class);

    private final CargaDemoServicio cargaDemoServicio;

    public DatosDemoConfig(CargaDemoServicio cargaDemoServicio) {
        this.cargaDemoServicio = cargaDemoServicio;
    }

    @Override
    public void run(String... args) {
        try {
            ResumenCargaDemo resumen = cargaDemoServicio.cargar();
            LOG.info("Carga de datos de demostración: {}", resumen.describir());
        } catch (Exception e) {
            // La aplicación sigue arrancando aunque la carga falle (todo o nada: no queda nada a medias)
            LOG.error("No se pudieron cargar los datos de demostración: {}", e.getMessage(), e);
        }
    }
}
