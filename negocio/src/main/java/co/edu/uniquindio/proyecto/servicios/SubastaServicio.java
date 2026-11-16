package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones básicas (CRUD) sobre subastas. Las reglas de crear, pujar y cerrar subastas se agregan después.
 */
public interface SubastaServicio {

    /**
     * Registra una subasta nueva.
     *
     * @throws NegocioException si el id viene informado y ya existe
     */
    Subasta registrarSubasta(Subasta s) throws NegocioException;

    /**
     * Actualiza una subasta.
     *
     * @throws NegocioException si el id es nulo o la subasta no existe
     */
    Subasta actualizarSubasta(Subasta s) throws NegocioException;

    /**
     * Obtiene una subasta por su id.
     *
     * @throws NegocioException si el id es nulo o la subasta no existe
     */
    Subasta obtenerSubasta(Long id) throws NegocioException;

    /**
     * Elimina una subasta por su id.
     *
     * @throws NegocioException si el id es nulo, la subasta no existe o ya tiene pujas
     */
    void eliminarSubasta(Long id) throws NegocioException;

    /** Lista todas las subastas. */
    List<Subasta> listarSubastas();

    /** Lista las subastas activas (fecha límite posterior a la fecha actual). */
    List<Subasta> listarActivas();
}
