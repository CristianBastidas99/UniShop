package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Table(name = "subasta_usuario")
public class SubastaUsuario implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "valor")
    private int valor;

    @Column(name = "fecha_subasta")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha_subasta;

    @ManyToOne
    @JoinColumn(name = "id_subasta")
    private Subasta id_subasta;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario id_usuario;

}
