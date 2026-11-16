package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre chats. Un comprador solo puede tener un chat por producto.
 */
public interface ChatServicio {

    /**
     * Registra un chat nuevo.
     *
     * @throws NegocioException si el id viene informado y ya existe, o si el comprador ya tiene
     *                          un chat sobre ese producto
     */
    Chat registrarChat(Chat c) throws NegocioException;

    /**
     * Actualiza un chat.
     *
     * @throws NegocioException si el id es nulo o el chat no existe
     */
    Chat actualizarChat(Chat c) throws NegocioException;

    /**
     * Obtiene un chat por su id.
     *
     * @throws NegocioException si el chat no existe
     */
    Chat obtenerChat(Long id) throws NegocioException;

    /**
     * Elimina un chat por su id.
     *
     * @throws NegocioException si el chat no existe
     */
    void eliminarChat(Long id) throws NegocioException;

    /** Lista todos los chats. */
    List<Chat> listarChat();

    /** Lista los chats en los que participa un usuario, como comprador o vendedor. */
    List<Chat> listarChatPersona(Long id);
}
