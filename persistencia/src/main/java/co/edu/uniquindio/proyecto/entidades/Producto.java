package co.edu.uniquindio.proyecto.entidades;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString
public class Producto implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @PositiveOrZero
    @Column(name = "unidades", nullable = false)
    private int unidades;

    @Size(max = 2000)
    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @NotNull
    @Positive
    @Column(name = "precio", nullable = false)
    private Double precio;

    @NotNull
    @Column(name = "fecha_limite", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaLimite;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(name = "descuento")
    private Double descuento;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @NotNull
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_ciudad")
    private Ciudad ciudad;

    @NotEmpty
    @ElementCollection
    @CollectionTable(name = "imagen", joinColumns = @JoinColumn(name = "id_producto", nullable = false))
    @Column(name = "ruta")
    private List<String> misImagenes;

    @ToString.Exclude
    @OneToMany(mappedBy = "producto")
    private List<Chat> misChats;

    @ToString.Exclude
    @OneToMany(mappedBy = "producto")
    private List<DetalleCompra> misDetalleCompras;

    @ToString.Exclude
    @OneToMany(mappedBy = "producto")
    private List<Comentario> misComentarios;

    @ToString.Exclude
    @OneToMany(mappedBy = "producto")
    private List<Subasta> misSubastas;

    @ToString.Exclude
    @ManyToMany(mappedBy = "misProductosFavoritos")
    private List<Usuario> misUsuariosFavoritos;

    @ToString.Exclude
    @ManyToMany(mappedBy = "misProductos")
    private List<Categoria> misCategorias;

    @ToString.Exclude
    @OneToMany(mappedBy = "producto")
    private List<HistorialPrecio> misHistorialPrecios;

    @ToString.Exclude
    @OneToMany(mappedBy = "producto")
    private List<ReporteProducto> misReportes;

}
