package co.edu.uniquindio.proyecto.dto.chat;

/**
 * Papel que tiene un usuario dentro de un chat: quien pregunta por el producto o quien lo vende.
 */
public enum RolChat {
    /** El usuario abrió el chat para preguntar por un producto de otra persona. */
    COMPRADOR,
    /** El usuario es el dueño del producto sobre el que se abrió el chat. */
    VENDEDOR
}
