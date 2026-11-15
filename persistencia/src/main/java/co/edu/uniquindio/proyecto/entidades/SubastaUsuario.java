package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.*;
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

    @NotNull
    @Positive
    @Column(name = "valor", nullable = false)
    private Double valor;

    @NotNull
    @Column(name = "fecha_subasta", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSubasta;

    @ManyToOne
    @JoinColumn(name = "id_subasta", nullable = false)
    @NotNull
    private Subasta subasta;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @NotNull
    private Usuario usuario;

}
