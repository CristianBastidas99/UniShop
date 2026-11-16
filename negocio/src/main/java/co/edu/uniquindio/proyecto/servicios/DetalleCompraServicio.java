package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre detalles de compra.
 */
public interface DetalleCompraServicio {

    /**
     * Registra el detalle de compra nuevo.
     *
     * @throws NegocioException si el id viene informado y ya existe el detalle de compra
     */
    DetalleCompra registrarDetalleCompra(DetalleCompra detalleCompra) throws NegocioException;

    /**
     * Actualiza el detalle de compra.
     *
     * @throws NegocioException si el id es nulo o el detalle de compra no existe
     */
    DetalleCompra actualizarDetalleCompra(DetalleCompra detalleCompra) throws NegocioException;

    /**
     * Obtiene el detalle de compra por su id.
     *
     * @throws NegocioException si el detalle de compra no existe
     */
    DetalleCompra obtenerDetalleCompra(Long id) throws NegocioException;

    /**
     * Elimina el detalle de compra por su id.
     *
     * @throws NegocioException si el detalle de compra no existe
     */
    void eliminarDetalleCompra(Long id) throws NegocioException;

    /** Lista todos los registros. */
    List<DetalleCompra> listarDetalleCompra();

}
