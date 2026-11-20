package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre cupones de descuento. El código del cupón es único.
 */
public interface CuponServicio {

    /**
     * Registra un cupón nuevo. El código se guarda en MAYÚSCULAS y sin espacios en los extremos; los usos
     * realizados quedan en 0.
     *
     * @throws NegocioException si el id viene informado y ya existe, si el código falta o ya está en uso, si el
     *                          porcentaje no está entre 1 % y 99 % (valor mayor que 0 y menor que 1), si la
     *                          fecha de vencimiento falta o no es futura, o si los usos máximos son menores que 1
     */
    Cupon registrarCupon(Cupon c) throws NegocioException;

    /**
     * Actualiza un cupón. El código se normaliza igual que al registrar; la fecha de vencimiento puede quedar en
     * el pasado (así se desactiva un cupón). Los usos realizados no se editan: se conservan los de la base.
     *
     * @throws NegocioException si el id es nulo, el cupón no existe, el código falta o pertenece a otro cupón,
     *                          el porcentaje no está entre 1 % y 99 %, falta la fecha de vencimiento o los usos
     *                          máximos son menores que los usos ya realizados
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
     * Obtiene un cupón por su código (sin distinguir mayúsculas ni espacios en los extremos).
     *
     * @throws NegocioException si no existe un cupón con ese código
     */
    Cupon obtenerPorCodigo(String codigo) throws NegocioException;

    /**
     * Obtiene un cupón que se puede usar hoy: existe, no está vencido y le quedan usos.
     * El código se normaliza como en {@link #obtenerPorCodigo(String)}.
     *
     * @throws NegocioException si no existe, está vencido o ya no tiene usos disponibles
     */
    Cupon obtenerDisponible(String codigo) throws NegocioException;
}
