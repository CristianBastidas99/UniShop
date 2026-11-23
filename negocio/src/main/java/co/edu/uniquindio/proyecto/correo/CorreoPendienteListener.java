package co.edu.uniquindio.proyecto.correo;

import co.edu.uniquindio.proyecto.config.ConfiguracionAsync;
import co.edu.uniquindio.proyecto.servicios.EmailServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía los correos pendientes solo después de confirmada la transacción que los publicó (o de inmediato si no
 * había transacción) y en segundo plano. Nunca lanza excepciones: un correo que falla no afecta la operación.
 */
@Component
public class CorreoPendienteListener {

    private static final Logger LOG = LoggerFactory.getLogger(CorreoPendienteListener.class);

    private final EmailServicio emailServicio;

    public CorreoPendienteListener(EmailServicio emailServicio) {
        this.emailServicio = emailServicio;
    }

    /**
     * Envía el correo del evento; si falla, solo lo registra.
     *
     * @param evento correo ya armado
     */
    @Async(ConfiguracionAsync.EJECUTOR_CORREO)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void enviar(CorreoPendienteEvent evento) {
        try {
            emailServicio.enviarCorreo(evento.getDestinatario(), evento.getAsunto(), evento.getHtml());
        } catch (Exception e) {
            LOG.error("No se envió el correo \"{}\": {}", evento.getAsunto(), e.getMessage());
        }
    }
}
