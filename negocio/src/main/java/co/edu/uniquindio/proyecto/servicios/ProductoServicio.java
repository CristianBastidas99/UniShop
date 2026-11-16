package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre productos.
 */
public interface ProductoServicio {

    /**
     * Registra el producto nuevo.
     *
     * @throws NegocioException si el id viene informado y ya existe el producto
     */
    Producto registrarProducto(Producto producto) throws NegocioException;

    /**
     * Actualiza el producto.
     *
     * @throws NegocioException si el id es nulo o el producto no existe
     */
    Producto actualizarProducto(Producto producto) throws NegocioException;

    /**
     * Obtiene el producto por su id.
     *
     * @throws NegocioException si el producto no existe
     */
    Producto obtenerProducto(Long id) throws NegocioException;

    /**
     * Elimina el producto por su id.
     *
     * @throws NegocioException si el producto no existe
     */
    void eliminarProducto(Long id) throws NegocioException;

    /** Lista todos los registros. */
    List<Producto> listarProducto();

    /** Lista los productos vigentes de una categoría. */
    List<Producto> listarProductoCategoria(Long idCategoria);

    /** Busca productos vigentes por texto. */
    List<Producto> listarProductoBusqueda(String busqueda);

    /** Lista los productos favoritos de un usuario (por su email). */
    List<Producto> listarProductoFavoritos(String email);
}
