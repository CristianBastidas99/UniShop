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

@Entity
@DiscriminatorValue("Usuario")
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class Usuario extends Persona implements Serializable {

    @ManyToOne
    @JoinColumn(name = "id_ciudad")
    @JsonIgnore
    private Ciudad id_ciudad;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_usuario")
    @JsonIgnore
    private List<Chat> misChats;

    @ElementCollection
    @CollectionTable(name = "telefono", joinColumns = @JoinColumn(name = "id_usuario", nullable = false))
    @Column(name = "num_telefono")
    private List<String> numeroTelefono;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_usuario")
    @JsonIgnore
    private List<Producto> misProductos;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_usuario")
    @JsonIgnore
    private List<Compra> misCompras;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_usuario")
    @JsonIgnore
    private List<Comentario> misComentarios;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_usuario")
    @JsonIgnore
    private List<SubastaUsuario> misSubastaUsuarios;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(name = "favorito", joinColumns = @JoinColumn(name = "id_producto", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "id_usuario", nullable = false))
    @JsonIgnore
    private List<Producto> misProductosFavoritos;

}
