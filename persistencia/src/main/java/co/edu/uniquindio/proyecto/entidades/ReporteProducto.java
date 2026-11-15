package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Necesidad extra 2: reporte (denuncia) de un producto.
 * Un usuario reporta un producto sospechoso (fraude, producto prohibido, etc.) y un administrador
 * lo revisa, cambia su estado y deja una respuesta. Un usuario solo puede reportar una vez el mismo producto.
 * Se llama "ReporteProducto" para no confundirlo con los reportes estadísticos del administrador.
 */
@Entity
@Table(name = "reporte_producto",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_producto"}))
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString
public class ReporteProducto implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 30)
    private MotivoReporte motivo;

    @Size(max = 1000)
    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @NotNull
    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Size(max = 1000)
    @Column(name = "respuesta_administrador", length = 1000)
    private String respuestaAdministrador;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @NotNull
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    @NotNull
    private Producto producto;

    /** Administrador que revisó el reporte (vacío mientras esté pendiente). */
    @ManyToOne
    @JoinColumn(name = "id_administrador")
    private Administrador administrador;

}
