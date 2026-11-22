package co.edu.uniquindio.proyecto.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuerpo del PATCH de descuento. Un descuento nulo o 0 quita el descuento; si no, debe estar en (0, 1).
 */
@Getter
@Setter
@NoArgsConstructor
public class DescuentoDTO {

    private Double descuento;
}
