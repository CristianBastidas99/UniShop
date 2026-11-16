package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre administradores. El correo es único entre usuarios y administradores.
 */
public interface AdministradorServicio {

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
