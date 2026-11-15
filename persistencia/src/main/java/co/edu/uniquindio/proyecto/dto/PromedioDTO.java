package co.edu.uniquindio.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Resultado de un reporte: un nombre (por ejemplo, un producto) y un promedio (por ejemplo, su calificación). */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class PromedioDTO {

    private String nombre;
    private Double promedio;
}
