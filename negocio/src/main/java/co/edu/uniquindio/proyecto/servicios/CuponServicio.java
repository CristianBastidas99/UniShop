package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre cupones de descuento. El código del cupón es único.
 */
public interface CuponServicio {

    /**
     * Registra un cupón nuevo.
     *
     * @throws NegocioException si el id viene informado y ya existe, o si el código ya está en uso
     */
    Cupon registrarCupon(Cupon c) throws NegocioException;

    /**
     * Actualiza un cupón.
     *
     * @throws NegocioException si el id es nulo, el cupón no existe o el código pertenece a otro cupón
     */
    Cupon actualizarCupon(Cupon c) throws NegocioException;

    /**
     * Obtiene un cupón por su id.
     *
     * @throws NegocioException si el id es nulo o el cupón no existe
     */
    Cupon obtenerCupon(Long id) throws NegocioException;

    /**
     * Elimina un cupón por su id.
     *
     * @throws NegocioException si el id es nulo, el cupón no existe o ya se usó en compras
     */
    void eliminarCupon(Long id) throws NegocioException;

    /** Lista todos los cupones. */
    List<Cupon> listarCupones();

    /**
     * Obtiene un cupón por su código.
     *
     * @throws NegocioException si no existe un cupón con ese código
     */
    Cupon obtenerPorCodigo(String codigo) throws NegocioException;

    /**
     * Obtiene un cupón que se puede usar hoy: existe, no está vencido y le quedan usos.
     *
     * @throws NegocioException si no existe, está vencido o ya no tiene usos disponibles
     */
    Cupon obtenerDisponible(String codigo) throws NegocioException;
}
