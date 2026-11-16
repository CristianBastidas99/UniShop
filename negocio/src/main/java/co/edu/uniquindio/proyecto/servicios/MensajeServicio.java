package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre mensajes.
 */
public interface MensajeServicio {

    /**
     * Registra el mensaje nuevo.
     *
     * @throws NegocioException si el id viene informado y ya existe el mensaje
     */
    Mensaje registrarMensaje(Mensaje mensaje) throws NegocioException;

    /**
     * Actualiza el mensaje.
     *
     * @throws NegocioException si el id es nulo o el mensaje no existe
     */
    Mensaje actualizarMensaje(Mensaje mensaje) throws NegocioException;

    /**
     * Obtiene el mensaje por su id.
     *
     * @throws NegocioException si el mensaje no existe
     */
    Mensaje obtenerMensaje(Long id) throws NegocioException;

    /**
     * Elimina el mensaje por su id.
     *
     * @throws NegocioException si el mensaje no existe
     */
    void eliminarMensaje(Long id) throws NegocioException;

    /** Lista todos los registros. */
    List<Mensaje> listarMensaje();

    /** Lista los mensajes de un chat. */
    List<Mensaje> listarMensajeChat(Long idChat);
}
