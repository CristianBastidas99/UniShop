package co.edu.uniquindio.proyecto.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta de la calificación de un producto: promedio (un decimal) y número de comentarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionDTO {

    private Long idProducto;
    private double promedio;
    private int cantidadComentarios;
}
