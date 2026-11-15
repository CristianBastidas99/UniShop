package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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

    @Column(name = "fecha_limite")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaLimite;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @OneToMany(mappedBy = "subasta")
    private List<SubastaUsuario> misSubastaUsuarios;

}
