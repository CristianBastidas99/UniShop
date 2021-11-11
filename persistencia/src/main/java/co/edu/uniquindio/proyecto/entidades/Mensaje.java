package co.edu.uniquindio.proyecto.entidades;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Mensaje implements Serializable {

    @Id
    private Long id;

    private String mensaje;

    private Date fecha;

    @ManyToOne
    private Chat miChat;

}
