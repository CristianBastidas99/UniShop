package co.edu.uniquindio.proyecto.entidades;

/**
 * Medios de pago aceptados en una {@link Compra}. Se guarda como texto en la base.
 */
public enum MedioPago {

    TARJETA("Tarjeta"), PAYPAL("PayPal"), PSE("PSE"), BALOTO("Baloto");

    private final String etiqueta;

    MedioPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Nombre en español para mostrar en la interfaz. */
    public String getEtiqueta() {
        return etiqueta;
    }

}
