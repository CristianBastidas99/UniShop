package co.edu.uniquindio.proyecto.entidades;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

//drop database unishop;create database unishop;use unishop;
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString
public class Persona implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    protected Long id;

    @Column(name = "nombre")
    protected String nombre;

    @Column(name = "email")
    protected String email;

    @Column(name = "password")
    protected String password;

}
