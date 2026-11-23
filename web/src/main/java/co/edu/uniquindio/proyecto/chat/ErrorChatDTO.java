package co.edu.uniquindio.proyecto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Error que se envía solo al usuario que lo causó, en {@code /user/queue/errores}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorChatDTO {

    private String mensaje;
}
