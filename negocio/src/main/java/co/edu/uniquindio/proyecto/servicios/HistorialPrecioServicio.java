package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.HistorialPrecio;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre el historial de precios de los productos.
 */
public interface HistorialPrecioServicio {

    /**
     * Registra un cambio de precio. Si la fecha viene nula se usa la fecha actual.
     *
     * @throws NegocioException si el id viene informado y ya existe
     */
    HistorialPrecio registrarHistorial(HistorialPrecio h) throws NegocioException;

    /**
     * Actualiza un registro del historial.
     *
     * @throws NegocioException si el id es nulo o el registro no existe
     */
    HistorialPrecio actualizarHistorial(HistorialPrecio h) throws NegocioException;

    /**
     * Obtiene un registro del historial por su id.
     *
     * @throws NegocioException si el id es nulo o el registro no existe
     */
    HistorialPrecio obtenerHistorial(Long id) throws NegocioException;

    /**
     * Elimina un registro del historial por su id.
     *
     * @throws NegocioException si el id es nulo o el registro no existe
     */
    void eliminarHistorial(Long id) throws NegocioException;

    /** Lista todos los registros del historial. */
    List<HistorialPrecio> listarHistoriales();

    /**
     * Lista los cambios de precio de un producto, del más reciente al más antiguo.
     *
     * @throws NegocioException si el id es nulo o el producto no existe
     */
    List<HistorialPrecio> listarPorProducto(Long idProducto) throws NegocioException;
}
