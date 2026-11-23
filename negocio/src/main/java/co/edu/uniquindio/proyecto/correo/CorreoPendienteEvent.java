package co.edu.uniquindio.proyecto.correo;

/**
 * Correo ya armado (destinatario, asunto y HTML) que se enviará cuando la transacción que lo publicó se confirme.
 * Es inmutable y no guarda entidades, así que se puede usar en otro hilo sin sesión de JPA.
 */
public final class CorreoPendienteEvent {

    private final String destinatario;
    private final String asunto;
    private final String html;

    public CorreoPendienteEvent(String destinatario, String asunto, String html) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.html = html;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getHtml() {
        return html;
    }
}
