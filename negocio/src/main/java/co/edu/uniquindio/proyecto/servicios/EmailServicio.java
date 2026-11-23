package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.excepciones.NegocioException;

/**
 * Envío de correos electrónicos en HTML (Fase 7). En desarrollo los correos llegan a Mailpit
 * (SMTP {@code localhost:1025}, interfaz en {@code http://localhost:8025}); el servidor se configura con
 * {@code spring.mail.*} y el remitente con {@code unishop.correo.remitente}.
 * <p>
 * El envío es <b>síncrono</b>: quien lo llama decide si lo hace en segundo plano o después de confirmar la
 * transacción. Un correo que falla nunca debe deshacer una operación que ya se hizo (por ejemplo una compra).
 */
public interface EmailServicio {

    /**
     * Envía un correo HTML en UTF-8 desde el remitente configurado.
     *
     * @param destinatario correo de quien lo recibe
     * @param asunto       asunto del correo
     * @param html         cuerpo en HTML; quien lo arma debe escapar los datos que vienen del usuario
     * @throws NegocioException si faltan datos o el servidor de correo no lo acepta
     */
    void enviarCorreo(String destinatario, String asunto, String html) throws NegocioException;
}
