package co.edu.uniquindio.proyecto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mensaje de chat que se publica en {@code /topic/chat/{idChat}}.
 * La fecha va en milisegundos y también ya formateada ({@code dd/MM/yyyy HH:mm}, hora de Bogotá).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MensajeChatDTO {

    private Long id;
    private Long idChat;
    private String texto;
    private long fecha;
    private String fechaTexto;
    private Long idRemitente;
    private String nombreRemitente;
}
