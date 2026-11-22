package co.edu.uniquindio.proyecto.excepciones;

/**
 * Excepción de negocio para cuando quien hace la acción no tiene permiso sobre el recurso
 * (por ejemplo, un usuario que no es el vendedor intenta modificar o eliminar un producto).
 * Extiende {@link NegocioException}, así que todo código que atrape {@code NegocioException} la sigue atrapando.
 * La API REST la convierte en un 403.
 * <p>
 * Regla: usarla en toda validación de "solo el dueño / solo los participantes" que se exponga por REST.
 * Hoy la usan las validaciones de dueño de {@code ProductoServicioImpl}.
 */
public class AccesoDenegadoException extends NegocioException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
