package co.edu.uniquindio.proyecto.entidades;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Mensaje enviado dentro de un {@link Chat}, con su fecha y el {@link Usuario} que lo envía
 * (el comprador del chat o el vendedor del producto).
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString
public class Mensaje implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "mensaje", nullable = false, length = 1000)
    private String mensaje;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private Date fecha;

    @ManyToOne
    @JoinColumn(name = "id_chat", nullable = false)
    @NotNull
    private Chat chat;

    /**
     * Usuario que envía el mensaje: el comprador del chat o el vendedor del producto.
     */
    @ManyToOne
    @JoinColumn(name = "id_remitente", nullable = false)
    @NotNull
    private Usuario remitente;

}
