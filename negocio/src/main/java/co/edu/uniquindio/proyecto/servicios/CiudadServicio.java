package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre ciudades. El nombre de la ciudad es único.
 */
public interface CiudadServicio {

    /**
     * Registra una ciudad nueva.
     *
     * @throws NegocioException si el id viene informado y ya existe, o si el nombre ya está en uso
     */
    Ciudad registrarCiudad(Ciudad c) throws NegocioException;

    /**
     * Actualiza una ciudad.
     *
     * @throws NegocioException si el id es nulo, la ciudad no existe o el nuevo nombre
     *                          pertenece a otra ciudad
     */
    Ciudad actualizarCiudad(Ciudad c) throws NegocioException;

    /**
     * Obtiene una ciudad por su id.
     *
     * @throws NegocioException si el id es nulo o la ciudad no existe
     */
    Ciudad obtenerCiudad(Long id) throws NegocioException;

    /**
     * Elimina una ciudad por su id.
     *
     * @throws NegocioException si el id es nulo, la ciudad no existe o tiene usuarios o productos asociados
     */
    void eliminarCiudad(Long id) throws NegocioException;

    /** Lista todas las ciudades. */
    List<Ciudad> listarCiudades();
}
