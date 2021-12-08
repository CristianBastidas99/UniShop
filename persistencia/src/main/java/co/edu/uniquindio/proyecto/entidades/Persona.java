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
    @Column(name = "id", nullable = false)
    protected Long id;

    @Column(name = "nombre", nullable = false)
    protected String nombre;

    @Column(name = "email", unique = true, nullable = false)
    protected String email;

    @Column(name = "password",nullable = false)
    protected String password;

}
