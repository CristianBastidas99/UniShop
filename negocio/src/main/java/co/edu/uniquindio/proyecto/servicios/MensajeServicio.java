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

    /**
     * Envía un mensaje al chat: guarda el texto sin espacios sobrantes, la fecha actual y el remitente.
     *
     * @throws NegocioException si el chat o el remitente no existen, si el remitente no es el comprador
     *                          ni el vendedor, o si el texto está vacío o supera los 1000 caracteres
     */
    Mensaje enviarMensaje(Long idChat, Long idRemitente, String texto) throws NegocioException;

    /**
     * Lista los mensajes del chat, del más antiguo al más reciente, para un participante.
     *
     * @throws NegocioException si el chat no existe o el usuario no es el comprador ni el vendedor
     */
    List<Mensaje> listarMensajes(Long idChat, Long idUsuario) throws NegocioException;
}
