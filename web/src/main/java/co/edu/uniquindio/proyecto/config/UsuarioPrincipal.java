package co.edu.uniquindio.proyecto.config;

import java.security.Principal;

/**
 * Identidad de la conexión WebSocket: su nombre es el id del usuario autenticado.
 */
public class UsuarioPrincipal implements Principal {

    private final String nombre;

    /** @param idUsuario id del usuario autenticado */
    public UsuarioPrincipal(Long idUsuario) {
        this.nombre = String.valueOf(idUsuario);
    }

    @Override
    public String getName() {
        return nombre;
    }

    @Override
    public String toString() {
        return "UsuarioPrincipal[" + nombre + "]";
    }
}
