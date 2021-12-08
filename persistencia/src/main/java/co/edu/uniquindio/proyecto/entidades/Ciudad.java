package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Ciudad implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "nombre")
    @EqualsAndHashCode.Include
    private String nombre;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_ciudad")
    private List<Producto> misProductos;

    @ToString.Exclude
    @OneToMany(mappedBy = "id_ciudad")
    private List<Usuario> misUsuarios;

}
