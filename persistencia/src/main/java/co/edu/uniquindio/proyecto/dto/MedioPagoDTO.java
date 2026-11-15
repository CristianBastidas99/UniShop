package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.entidades.MedioPago;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Resultado del reporte de compras por medio de pago. */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MedioPagoDTO {

    private MedioPago medioPago;
    private Long cantidad;
}
