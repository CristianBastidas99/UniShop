package co.edu.uniquindio.proyecto.rest.dto.usuario;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de la API con los datos públicos de un usuario. Nunca incluye la contraseña.
 */
@Getter
public class UsuarioDTO {

    private final Long id;
    private final String nombre;
    private final String email;
    private final Long idCiudad;
    private final String nombreCiudad;
    private final List<String> telefonos;
    private final boolean activo;

    private UsuarioDTO(Usuario u) {
        this.id = u.getId();
        this.nombre = u.getNombre();
        this.email = u.getEmail();
        this.idCiudad = u.getCiudad() == null ? null : u.getCiudad().getId();
        this.nombreCiudad = u.getCiudad() == null ? null : u.getCiudad().getNombre();
        this.telefonos = u.getNumeroTelefono() == null ? new ArrayList<>() : new ArrayList<>(u.getNumeroTelefono());
        this.activo = u.isActivo();
    }

    /** Convierte la entidad en DTO; debe llamarse con la sesión de JPA abierta (teléfonos y ciudad son perezosos). */
    public static UsuarioDTO desde(Usuario u) {
        return new UsuarioDTO(u);
    }
}
