package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Subasta de un {@link Producto} hasta una fecha límite. Las pujas están en {@link SubastaUsuario};
 * al vencer, la puja más alta gana.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Subasta implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(name = "fecha_limite", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaLimite;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    @NotNull
    private Producto producto;

    /** Indica si la subasta ya se cerró (se procesó el resultado al vencer la fecha límite). */
    @Column(name = "cerrada", nullable = false)
    private boolean cerrada;

    /** Compra generada para el ganador al cerrar la subasta; es nula si no hubo pujas o no había unidades. */
    @OneToOne
    @JoinColumn(name = "id_compra")
    private Compra compra;

    @OneToMany(mappedBy = "subasta")
    private List<SubastaUsuario> misSubastaUsuarios;

}
