package co.edu.uniquindio.proyecto.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * Datos comunes de Usuario y Administrador. Es abstracta: no tiene tabla propia (herencia TABLE_PER_CLASS),
 * así que los ids de usuario y administrador salen de la misma secuencia y no se repiten entre las dos tablas.
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString
public abstract class Persona implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    protected Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    protected String nombre;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", unique = true, nullable = false, length = 100)
    protected String email;

    /** Hash BCrypt de la contraseña (nunca el texto plano). Se excluye de toString y de la serialización JSON. */
    @NotBlank
    @Column(name = "password", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    protected String password;

}
