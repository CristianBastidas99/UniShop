package co.edu.uniquindio.proyecto.excepciones;

/**
 * Excepción de negocio para cuando el recurso que se pidió (por id o por correo) no existe.
 * Extiende {@link NegocioException}, así que todo código que atrape {@code NegocioException} la sigue atrapando.
 * La API REST la convierte en un 404; una {@code NegocioException} común se convierte en un 400.
 * <p>
 * Solo se usa para el recurso principal (por ejemplo, el producto de {@code GET /api/productos/{id}}).
 * Si lo que no existe es un dato referenciado dentro del cuerpo (la ciudad o una categoría de un producto
 * nuevo), se lanza {@code NegocioException}, porque son datos inválidos (400).
 */
public class NoEncontradoException extends NegocioException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
