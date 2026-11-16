package co.edu.uniquindio.proyecto.tareas;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita las tareas programadas. Se puede desactivar con {@code unishop.tareas.habilitadas=false}
 * (por ejemplo, en las pruebas).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "unishop.tareas.habilitadas", havingValue = "true", matchIfMissing = true)
public class ConfiguracionTareas {
}
