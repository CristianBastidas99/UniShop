package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.ItemCompraDTO;
import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
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

    /**
     * Realiza una compra: crea la compra con la fecha actual y sus detalles, guarda el precio con descuento
     * (y con el cupón prorrateado, si hay), descuenta las unidades y suma un uso al cupón. Todo ocurre en una
     * transacción y se valida todo antes de escribir.
     *
     * @param idUsuario    comprador
     * @param items        productos y unidades; si un producto se repite se suma en un solo detalle
     * @param medioPago    medio de pago
     * @param codigoCupon código del cupón (opcional; nulo o en blanco significa sin cupón)
     * @throws NegocioException si el usuario, el medio de pago, algún producto o el cupón no son válidos,
     *                          o si no hay suficientes unidades
     */
    Compra comprar(Long idUsuario, List<ItemCompraDTO> items, MedioPago medioPago, String codigoCupon)
            throws NegocioException;

    /**
     * Lista las compras del usuario, la más reciente primero.
     *
     * @throws NegocioException si el usuario no existe
     */
    List<Compra> listarComprasUsuario(Long idUsuario) throws NegocioException;

    /**
     * Lista los detalles de una compra.
     *
     * @throws NegocioException si la compra no existe
     */
    List<DetalleCompra> listarDetalles(Long idCompra) throws NegocioException;

    /**
     * Calcula el total de la compra: suma de unidades por precio guardado en cada detalle, con 2 decimales.
     *
     * @throws NegocioException si la compra no existe
     */
    double calcularTotal(Long idCompra) throws NegocioException;

}
