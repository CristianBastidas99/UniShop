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

    /**
     * Devuelve el chat del comprador sobre el producto, y lo crea si todavía no existe.
     * Un chat existente se devuelve aunque el producto ya esté vencido; uno nuevo solo se crea
     * si el producto está vigente.
     *
     * @throws NegocioException si el comprador o el producto no existen, si el comprador es el
     *                          vendedor del producto, o si hay que crear el chat y el producto no está disponible
     */
    Chat obtenerOCrearChat(Long idComprador, Long idProducto) throws NegocioException;

    /**
     * Lista los chats de un usuario, como comprador y como vendedor.
     *
     * @throws NegocioException si el usuario no existe
     */
    List<Chat> listarChatsUsuario(Long idUsuario) throws NegocioException;

    /**
     * Indica si el usuario es el comprador del chat o el vendedor del producto.
     * Devuelve {@code false} si algún dato es nulo o el chat no existe.
     */
    boolean esParticipante(Long idChat, Long idUsuario);
}
