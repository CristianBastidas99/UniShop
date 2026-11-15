package co.edu.uniquindio.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Resultado del reporte de ventas: año, mes (1 a 12) y total vendido en ese mes. */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class VentaMesDTO {

    private Integer anio;
    private Integer mes;
    private Double total;
}
