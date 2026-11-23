package co.edu.uniquindio.proyecto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Habilita la ejecución en segundo plano ({@code @Async}) y define el ejecutor pequeño que usa el envío de
 * correos, para no demorar al comprador. Es independiente de {@code unishop.tareas.habilitadas}, que solo
 * controla las tareas programadas.
 */
@Configuration
@EnableAsync
public class ConfiguracionAsync {

    /** Nombre del ejecutor que se usa en {@code @Async("ejecutorCorreo")}. */
    public static final String EJECUTOR_CORREO = "ejecutorCorreo";

    /**
     * Ejecutor de los correos: 2 hilos "correo-", cola de 100 y, si se llena, el envío se hace en el hilo que lo pide
     * (así no se pierde el correo).
     */
    @Bean(name = EJECUTOR_CORREO)
    public Executor ejecutorCorreo() {
        ThreadPoolTaskExecutor ejecutor = new ThreadPoolTaskExecutor();
        ejecutor.setCorePoolSize(2);
        ejecutor.setMaxPoolSize(2);
        ejecutor.setQueueCapacity(100);
        ejecutor.setThreadNamePrefix("correo-");
        ejecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ejecutor.setWaitForTasksToCompleteOnShutdown(true);
        ejecutor.setAwaitTerminationSeconds(10);
        return ejecutor;
    }
}
