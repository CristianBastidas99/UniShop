package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Necesidad extra 1: historial de precios de un producto.
 * Cada vez que el vendedor cambia el precio o el descuento de un producto se guarda un registro
 * con los valores anteriores y los nuevos. Sirve para mostrar rebajas ("antes $X, ahora $Y")
 * y para reportes de evolución de precios.
 */
@Entity
@Table(name = "historial_precio")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString
public class HistorialPrecio implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Positive
    @Column(name = "precio_anterior", nullable = false)
    private Double precioAnterior;

    @NotNull
    @Positive
    @Column(name = "precio_nuevo", nullable = false)
    private Double precioNuevo;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(name = "descuento_anterior")
    private Double descuentoAnterior;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(name = "descuento_nuevo")
    private Double descuentoNuevo;

    @NotNull
    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    @NotNull
    private Producto producto;

}
