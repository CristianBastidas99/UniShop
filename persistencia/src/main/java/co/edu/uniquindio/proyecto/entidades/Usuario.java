package co.edu.uniquindio.proyecto.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Usuario de la tienda (hereda de {@link Persona}): puede vender y comprar productos.
 * Relaciones: vive en una {@link Ciudad}, tiene varios teléfonos (tabla {@code telefono}), publica
 * {@link Producto}s, hace {@link Compra}s, escribe {@link Comentario}s, abre {@link Chat}s como comprador,
 * puja en subastas ({@link SubastaUsuario}), reporta productos ({@link ReporteProducto}) y tiene una lista de
 * productos favoritos (tabla {@code favorito}, el dueño de la relación es Usuario).
 */
@Entity
@DiscriminatorValue("Usuario")
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Usuario extends Persona implements Serializable {

    /**
     * Indica si la cuenta está activa. Un usuario desactivado por el administrador no puede iniciar sesión.
     * El {@code DEFAULT 1} hace que las filas existentes queden activas al actualizar el esquema.
     */
    @Column(name = "activo", nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    private boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "id_ciudad")
    @JsonIgnore
    private Ciudad ciudad;

    @ToString.Exclude
    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Chat> misChats;

    @ElementCollection
    @CollectionTable(name = "telefono", joinColumns = @JoinColumn(name = "id_usuario", nullable = false))
    @Column(name = "num_telefono")
    private List<String> numeroTelefono;

    @ToString.Exclude
    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Producto> misProductos;

    @ToString.Exclude
    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Compra> misCompras;

    @ToString.Exclude
    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Comentario> misComentarios;

    @ToString.Exclude
    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<SubastaUsuario> misSubastaUsuarios;

    @ToString.Exclude
    @ManyToMany
    // La restricción única impide a nivel de BD que un producto aparezca dos veces como favorito del mismo usuario
    @JoinTable(name = "favorito", joinColumns = @JoinColumn(name = "id_usuario", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "id_producto", nullable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_producto"}))
    @JsonIgnore
    private List<Producto> misProductosFavoritos;

    @ToString.Exclude
    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<ReporteProducto> misReportes;

}
