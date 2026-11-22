package co.edu.uniquindio.proyecto.rest.dto.usuario;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Cuerpo de {@code POST} y {@code PUT} de usuarios. Correo y contraseña son obligatorios al crear y
 * opcionales al actualizar (la contraseña, si no viene, conserva la actual); esa regla se valida en el
 * controlador. En el {@code PUT} el correo del cuerpo se ignora: manda el de la ruta.
 */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEntradaDTO {

    @NotBlank(message = "no debe estar vacío")
    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String nombre;

    @Email(message = "no es un correo válido")
    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String email;

    @Size(max = 100, message = "no debe superar 100 caracteres")
    private String password;

    @NotNull(message = "es obligatoria")
    private Long idCiudad;

    @NotEmpty(message = "debe tener al menos un teléfono")
    private List<String> telefonos;
}
