package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre comentarios.
 */
public interface ComentarioServicio {

    /**
     * Registra el comentario nuevo.
     *
     * @throws NegocioException si el id viene informado y ya existe el comentario
     */
    Comentario registrarComentario(Comentario comentario) throws NegocioException;

    /**
     * Actualiza el comentario.
     *
     * @throws NegocioException si el id es nulo o el comentario no existe
     */
    Comentario actualizarComentario(Comentario comentario) throws NegocioException;

    /**
     * Obtiene el comentario por su id.
     *
     * @throws NegocioException si el comentario no existe
     */
    Comentario obtenerComentario(Long id) throws NegocioException;

    /**
     * Elimina el comentario por su id.
     *
     * @throws NegocioException si el comentario no existe
     */
    void eliminarComentario(Long id) throws NegocioException;

    /** Lista todos los registros. */
    List<Comentario> listarComentario();

    /** Lista los comentarios de un producto. */
    List<Comentario> listarComentarioProducto(Long idProducto);

    /** Lista los comentarios hechos por un usuario. */
    List<Comentario> listarComentarioUsuario(Long idUsuario);

    /**
     * Comenta y califica (de 0 a 5) un producto. Un usuario puede comentar el mismo producto varias
     * veces (decisión de diseño: cada comentario cuenta en el promedio). La fecha se asigna con el
     * momento actual, el id y la respuesta se ignoran (quedan nulos).
     *
     * @throws NegocioException si falta el usuario o el producto, no existen, el mensaje está vacío o
     *                          supera 1000 caracteres, la calificación está fuera de 0 a 5 o el usuario
     *                          es el vendedor del producto
     */
    Comentario comentarProducto(Comentario comentario) throws NegocioException;

    /**
     * Permite al vendedor responder un comentario de su producto. La respuesta no se puede modificar
     * una vez registrada (decisión de diseño).
     *
     * @throws NegocioException si el comentario o el vendedor no existen, quien responde no es el
     *                          dueño del producto, la respuesta está vacía o supera 255 caracteres,
     *                          o el comentario ya tiene respuesta
     */
    Comentario responderComentario(Long idComentario, String respuesta, Long idVendedor) throws NegocioException;

    /**
     * Calcula la calificación promedio de un producto, redondeada a un decimal (HALF_UP).
     * Devuelve 0 si el producto no tiene comentarios.
     *
     * @throws NegocioException si el producto no existe
     */
    double calcularCalificacion(Long idProducto) throws NegocioException;
}
