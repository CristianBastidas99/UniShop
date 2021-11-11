package co.edu.uniquindio.proyecto.entidades;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto implements Serializable{

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @Column
    private String nombre;

    private int unidades;

    private String descripcion;

    private int precio;

    private Date fechaLimite;

    private Double descuento;

    @OneToMany(mappedBy = "id_producto")
    private List<Chat> misChats;


}
