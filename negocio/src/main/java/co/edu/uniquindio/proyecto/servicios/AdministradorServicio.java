package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre administradores. El correo es único entre usuarios y administradores.
 */
public interface AdministradorServicio {

    /**
     * Inicia sesión: busca por correo (sin espacios en los extremos y en minúsculas) y contraseña.
     * Si el correo no existe o la contraseña es errónea se lanza el mismo mensaje, para no revelar
     * si el correo está registrado.
     *
     * @throws NegocioException si el correo o la contraseña vienen nulos o vacíos, o si las credenciales
     *                          son incorrectas
     */
    Administrador iniciarSesion(String email, String password) throws NegocioException;

    /**
     * Registra un administrador nuevo.
     *
     * @throws NegocioException si el id viene informado y ya existe, o si el correo ya está
     *                          registrado (como usuario o como administrador)
     */
    Administrador registrarAdministrador(Administrador a) throws NegocioException;

    /**
     * Actualiza un administrador buscándolo por su correo.
     *
     * @throws NegocioException si no existe un administrador con ese correo
     */
    Administrador actualizarAdministrador(Administrador a) throws NegocioException;

    /**
     * Obtiene un administrador por su correo.
     *
     * @throws NegocioException si no existe un administrador con ese correo
     */
    Administrador obtenerAdministrador(String email) throws NegocioException;

    /**
     * Elimina un administrador por su correo.
     *
     * @throws NegocioException si no existe un administrador con ese correo
     */
    void eliminarAdministrador(String email) throws NegocioException;

    /** Lista todos los administradores. */
    List<Administrador> listarAdministrador();
}
