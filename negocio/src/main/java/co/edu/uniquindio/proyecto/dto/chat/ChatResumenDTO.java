package co.edu.uniquindio.proyecto.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Resumen de un chat para la lista "Mis chats": producto, la otra persona y el último mensaje.
 * Son datos simples (no entidades), así que se pueden usar fuera de la transacción.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResumenDTO implements Serializable {

    /** Id del chat. */
    private Long idChat;

    /** Papel del usuario consultado en este chat. */
    private RolChat rol;

    /** Id de la otra persona del chat. */
    private Long idOtraPersona;

    /** Nombre de la otra persona del chat. */
    private String nombreOtraPersona;

    /** Id del producto sobre el que trata el chat. */
    private Long idProducto;

    /** Nombre del producto. */
    private String nombreProducto;

    /** Ruta de la imagen principal del producto (null si no tiene imágenes). */
    private String imagenProducto;

    /** Texto del último mensaje, recortado (null si el chat no tiene mensajes). */
    private String ultimoMensaje;

    /** Fecha del último mensaje (null si el chat no tiene mensajes). */
    private Date fechaUltimoMensaje;

    /** Indica si el último mensaje lo envió el usuario consultado. */
    private boolean ultimoMensajePropio;
}
