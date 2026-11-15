package co.edu.uniquindio.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Resultado de un reporte: un nombre (categoría, vendedor...) y una cantidad. */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CantidadDTO {

    private String nombre;
    private Long cantidad;
}
