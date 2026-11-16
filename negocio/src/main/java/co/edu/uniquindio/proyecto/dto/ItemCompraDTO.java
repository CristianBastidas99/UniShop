package co.edu.uniquindio.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Producto y cantidad que el usuario quiere comprar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCompraDTO {

    /** Id del producto que se quiere comprar. */
    private Long idProducto;

    /** Unidades que se quieren comprar. */
    private int unidades;
}
