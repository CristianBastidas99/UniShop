package co.edu.uniquindio.proyecto.tareas;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cierra periódicamente las subastas vencidas y genera la compra del ganador. Cada subasta se cierra en su
 * propia transacción; si una falla se registra el error y se continúa con las demás.
 */
@Component
public class CierreSubastasTarea {

    private static final Logger LOG = LoggerFactory.getLogger(CierreSubastasTarea.class);

    private final SubastaServicio subastaServicio;

    public CierreSubastasTarea(SubastaServicio subastaServicio) {
        this.subastaServicio = subastaServicio;
    }

    /**
     * Cierra todas las subastas vencidas sin cerrar. Se ejecuta un minuto después de terminar la ejecución anterior.
     *
     * @return cantidad de subastas cerradas correctamente
     */
    @Scheduled(fixedDelay = 60000)
    public int cerrarSubastasVencidas() {
        int cerradas = 0;
        for (Subasta subasta : subastaServicio.listarVencidasSinCerrar()) {
            try {
                subastaServicio.cerrarSubasta(subasta.getId());
                cerradas++;
            } catch (Exception e) {
                LOG.error("No se pudo cerrar la subasta {}: {}", subasta.getId(), e.getMessage());
            }
        }
        return cerradas;
    }
}
