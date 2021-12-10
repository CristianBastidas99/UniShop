package co.edu.uniquindio.proyecto.entidades;

import lombok.*;

import javax.persistence.*;
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

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "unidades")
    private int unidades;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio")
    private int precio;

    @Column(name = "fecha_limite")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha_limite;

    @Column(name = "descuento")
    private Double descuento;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario id_usuario;

    @ManyToOne
    @JoinColumn(name = "id_ciudad")
    private Ciudad id_ciudad;

    @ElementCollection
    @CollectionTable(name = "imagen", joinColumns = @JoinColumn(name = "id_producto", nullable = false))
    @Column(name = "ruta")
    private List<String> misImagenes;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_producto")
    private List<Chat> misChats;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_producto")
    private List<DetalleCompra> misDetalleCompras;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_producto")
    private List<Comentario> misComentarios;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_producto")
    private List<Subasta> misSubastas;

    @ToString.Exclude
    @ManyToMany(mappedBy = "misProductosFavoritos")
    private List<Usuario> misUsuariosFavoritos;

    @ToString.Exclude
    @ManyToMany(mappedBy = "misProductos")
    private List<Categoria> misCategorias;



}
