package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

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

    @Column(name = "unidades")
    private int unidades;

    @Column(name = "precio_producto")
    private int precio_producto;

    @ManyToOne
    @JoinColumn(name = "id_compra")
    private Compra id_compra;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto id_producto;
}
