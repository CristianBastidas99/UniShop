package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones básicas (CRUD) sobre las pujas de las subastas. Las reglas de validación de la puja
 * (por ejemplo, que sea mayor a la más alta) se agregan después.
 */
public interface SubastaUsuarioServicio {

    /**
     * Registra una puja validando las reglas de la subasta: debe estar activa, el vendedor no puede pujar y el
     * valor debe superar la puja más alta (o ser mayor a cero si aún no hay pujas).
     *
     * @throws NegocioException si la subasta o el usuario no existen, la subasta no está activa, el usuario es
     *                          el vendedor o el valor no supera la puja más alta
     */
    SubastaUsuario pujar(Long idSubasta, Long idUsuario, double valor) throws NegocioException;

    /**
     * Registra una puja nueva.
     *
     * @throws NegocioException si el id viene informado y ya existe
     */
    SubastaUsuario registrarPuja(SubastaUsuario p) throws NegocioException;

    /**
     * Actualiza una puja.
     *
     * @throws NegocioException si el id es nulo o la puja no existe
     */
    SubastaUsuario actualizarPuja(SubastaUsuario p) throws NegocioException;

    /**
     * Obtiene una puja por su id.
     *
     * @throws NegocioException si el id es nulo o la puja no existe
     */
    SubastaUsuario obtenerPuja(Long id) throws NegocioException;

    /**
     * Elimina una puja por su id.
     *
     * @throws NegocioException si el id es nulo o la puja no existe
     */
    void eliminarPuja(Long id) throws NegocioException;

    /** Lista todas las pujas. */
    List<SubastaUsuario> listarPujas();

    /**
     * Lista las pujas de una subasta, de la mayor a la menor.
     *
     * @throws NegocioException si el id es nulo o la subasta no existe
     */
    List<SubastaUsuario> listarPorSubasta(Long idSubasta) throws NegocioException;

    /**
     * Devuelve el valor de la puja más alta de una subasta.
     *
     * @return el valor más alto, o 0.0 si la subasta no tiene pujas
     * @throws NegocioException si el id es nulo o la subasta no existe
     */
    Double obtenerPujaMasAlta(Long idSubasta) throws NegocioException;
}
