package co.edu.uniquindio.proyecto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Cuerpo JSON de los mensajes y errores de la API REST: {@code { "mensaje": "..." }}.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Mensaje {

    /** Texto que se le muestra al cliente de la API. */
    private String mensaje;
}
