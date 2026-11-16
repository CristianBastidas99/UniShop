package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * Línea de una {@link Compra}: qué {@link Producto} se compró, cuántas unidades y a qué precio.
 * El precio se guarda al momento de la compra (ya con el descuento del producto y, si la compra usó un cupón,
 * con el cupón prorrateado) para que no cambie si después cambia el precio del producto.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class DetalleCompra implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @Positive
    @Column(name = "unidades", nullable = false)
    private int unidades;

    @NotNull
    @Positive
    @Column(name = "precio_producto", nullable = false)
    private Double precioProducto;

    @ManyToOne
    @JoinColumn(name = "id_compra", nullable = false)
    @NotNull
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    @NotNull
    private Producto producto;
}
