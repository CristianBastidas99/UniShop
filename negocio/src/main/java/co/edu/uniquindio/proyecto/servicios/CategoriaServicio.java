package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre categorías. El nombre de la categoría es único.
 */
public interface CategoriaServicio {

    /**
     * Registra una categoría nueva.
     *
     * @throws NegocioException si el id viene informado y ya existe, o si el nombre ya está en uso
     */
    Categoria registrarCategoria(Categoria c) throws NegocioException;

    /**
     * Actualiza una categoría.
     *
     * @throws NegocioException si el id es nulo, la categoría no existe o el nuevo nombre
     *                          pertenece a otra categoría
     */
    Categoria actualizarCategoria(Categoria c) throws NegocioException;

    /**
     * Obtiene una categoría por su id.
     *
     * @throws NegocioException si la categoría no existe
     */
    Categoria obtenerCategoria(Long id) throws NegocioException;

    /**
     * Elimina una categoría por su id.
     *
     * @throws NegocioException si la categoría no existe
     */
    void eliminarCategoria(Long id) throws NegocioException;

    /** Lista todas las categorías. */
    List<Categoria> listarCategoria();
}
