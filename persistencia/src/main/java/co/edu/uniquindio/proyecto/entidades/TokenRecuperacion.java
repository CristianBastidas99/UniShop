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
 * Token de recuperación de contraseña (Paso 7.3). El token que se envía por correo <b>nunca se guarda en claro</b>:
 * solo se guarda su hash SHA-256 en hexadecimal, así que quien lea la base no puede usarlo.
 * No se relaciona con {@link Persona}: el correo es único entre usuarios y administradores, y con la herencia
 * TABLE_PER_CLASS una llave foránea a la persona no es posible; por eso se guarda el correo.
 * Un token sirve una sola vez y vence en la fecha de expiración.
 */
@Entity
@Table(name = "token_recuperacion")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString(exclude = "tokenHash")
public class TokenRecuperacion implements Serializable {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    /** Hash SHA-256 del token, en hexadecimal (64 caracteres). */
    @NotBlank
    @Size(min = 64, max = 64)
    @Column(name = "token_hash", unique = true, nullable = false, length = 64)
    private String tokenHash;

    /** Correo (en minúsculas) de la persona que pidió recuperar la contraseña. */
    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @NotNull
    @Column(name = "fecha_creacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @NotNull
    @Column(name = "fecha_expiracion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaExpiracion;

    /** Indica si el token ya se usó o fue invalidado por uno más reciente. */
    @Column(name = "usado", nullable = false)
    private boolean usado;

    /** Momento en que se usó o invalidó (vacío mientras no se haya usado). */
    @Column(name = "fecha_uso")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaUso;
}
