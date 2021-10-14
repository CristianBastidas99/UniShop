package co.edu.uniquindio.proyecto.entidades;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public class Usuario implements Serializable{

    @Id
    @GeneratedValue
    private Long id;

    private String nombreCompleto;

    private String email;

    private String contraseña;

    @ElementCollection
    private List<String> numeroTelefono;

    @ElementCollection
    private Map<String, String> ubicacion;


    public Usuario( String nombreCompleto, String email, String contraseña) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.contraseña = contraseña;
        this.numeroTelefono = null;
        this.ubicacion = null;
    }
}
