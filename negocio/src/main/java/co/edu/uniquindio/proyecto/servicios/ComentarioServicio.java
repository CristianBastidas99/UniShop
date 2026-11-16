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
}
