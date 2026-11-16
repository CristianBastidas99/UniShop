package co.edu.uniquindio.proyecto.excepciones;

/**
 * Excepción (verificada) que lanzan los servicios de negocio cuando se incumple una regla,
 * por ejemplo cuando una entidad ya existe, no existe o tiene un dato duplicado.
 */
public class NegocioException extends Exception {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
