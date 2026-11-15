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
import java.util.List;

/**
 * Necesidad extra 3: cupón de descuento.
 * Tiene un código único, un porcentaje de descuento sobre el total de la compra, una fecha de
 * vencimiento y un número máximo de usos. Una compra puede usar como máximo un cupón.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString
public class Cupon implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    private String codigo;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax("1.0")
    @Column(name = "porcentaje", nullable = false)
    private Double porcentaje;

    @NotNull
    @Column(name = "fecha_vencimiento", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimiento;

    @Positive
    @Column(name = "usos_maximos", nullable = false)
    private int usosMaximos;

    @PositiveOrZero
    @Column(name = "usos_realizados", nullable = false)
    private int usosRealizados;

    @ToString.Exclude
    @OneToMany(mappedBy = "cupon")
    private List<Compra> misCompras;

}
