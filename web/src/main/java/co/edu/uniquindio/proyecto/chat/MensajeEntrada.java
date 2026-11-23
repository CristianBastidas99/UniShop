package co.edu.uniquindio.proyecto.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuerpo JSON que envía el cliente al chat: {@code {"texto": "..."}}.
 */
@Getter
@Setter
@NoArgsConstructor
public class MensajeEntrada {

    /** Texto del mensaje (el servicio valida de 1 a 1000 caracteres). */
    private String texto;
}
