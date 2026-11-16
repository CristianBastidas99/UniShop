package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre compras.
 */
public interface CompraServicio {

    /**
     * Registra la compra nueva.
     *
     * @throws NegocioException si el id viene informado y ya existe la compra
     */
    Compra registrarCompra(Compra compra) throws NegocioException;

    /**
     * Actualiza la compra.
     *
     * @throws NegocioException si el id es nulo o la compra no existe
     */
    Compra actualizarCompra(Compra compra) throws NegocioException;

    /**
     * Obtiene la compra por su id.
     *
     * @throws NegocioException si la compra no existe
     */
    Compra obtenerCompra(Long id) throws NegocioException;

    /**
     * Elimina la compra por su id.
     *
     * @throws NegocioException si la compra no existe
     */
    void eliminarCompra(Long id) throws NegocioException;

    /** Lista todos los registros. */
    List<Compra> listarCompra();

}
