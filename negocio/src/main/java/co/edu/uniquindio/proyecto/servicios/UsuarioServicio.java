package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre usuarios. El correo es único entre usuarios y administradores.
 */
public interface UsuarioServicio {

    /**
     * Registra un usuario nuevo.
     *
     * @throws NegocioException si el id viene informado y ya existe, o si el correo ya está
     *                          registrado (como usuario o como administrador)
     */
    Usuario registrarUsuario(Usuario u) throws NegocioException;

    /**
     * Actualiza un usuario buscándolo por su correo.
     *
     * @throws NegocioException si no existe un usuario con ese correo
     */
    Usuario actualizarUsuario(Usuario u) throws NegocioException;

    /**
     * Obtiene un usuario por su correo.
     *
     * @throws NegocioException si no existe un usuario con ese correo
     */
    Usuario obtenerUsuario(String email) throws NegocioException;

    /**
     * Elimina un usuario por su correo.
     *
     * @throws NegocioException si no existe un usuario con ese correo
     */
    void eliminarUsuario(String email) throws NegocioException;

    /** Lista todos los usuarios. */
    List<Usuario> listarUsuarios();
}
