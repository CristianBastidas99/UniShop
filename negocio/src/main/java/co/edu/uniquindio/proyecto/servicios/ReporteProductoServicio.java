package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre los reportes de productos. Un usuario reporta un producto y un
 * administrador lo revisa, cambia su estado y deja una respuesta.
 */
public interface ReporteProductoServicio {

    /**
     * Registra un reporte tal como viene (CRUD básico).
     *
     * @throws NegocioException si el id viene informado y ya existe
     */
    ReporteProducto registrarReporte(ReporteProducto r) throws NegocioException;

    /**
     * Actualiza un reporte.
     *
     * @throws NegocioException si el id es nulo o el reporte no existe
     */
    ReporteProducto actualizarReporte(ReporteProducto r) throws NegocioException;

    /**
     * Obtiene un reporte por su id.
     *
     * @throws NegocioException si el id es nulo o el reporte no existe
     */
    ReporteProducto obtenerReporte(Long id) throws NegocioException;

    /**
     * Elimina un reporte por su id.
     *
     * @throws NegocioException si el id es nulo o el reporte no existe
     */
    void eliminarReporte(Long id) throws NegocioException;

    /** Lista todos los reportes. */
    List<ReporteProducto> listarReportes();

    /**
     * Crea un reporte de un usuario sobre un producto. Ignora la fecha y el estado recibidos:
     * la fecha es la actual y el estado queda PENDIENTE.
     *
     * @throws NegocioException si el usuario o el producto son nulos o no existen, si el usuario
     *                          es el vendedor del producto o si ya reportó ese producto antes
     */
    ReporteProducto reportarProducto(ReporteProducto reporte) throws NegocioException;

    /** Lista los reportes pendientes de revisión. */
    List<ReporteProducto> listarPendientes();

    /** Lista los reportes por revisar (PENDIENTE y EN_REVISION), el más antiguo primero. */
    List<ReporteProducto> listarPorRevisar();

    /** Lista los reportes ya resueltos (ACEPTADO y RECHAZADO), el más reciente primero. */
    List<ReporteProducto> listarResueltos();

    /** Indica si el usuario ya reportó el producto (false si alguno de los ids es nulo). */
    boolean yaReporto(Long idUsuario, Long idProducto);

    /** Lista los reportes de un producto. */
    List<ReporteProducto> listarPorProducto(Long idProducto);

    /**
     * Cambia el estado de un reporte registrando la respuesta y el administrador que lo revisó.
     * Si el nuevo estado es ACEPTADO, el producto reportado se invalida (su fecha límite pasa a ser ahora,
     * solo si era posterior) para que deje de aparecer en los listados públicos.
     *
     * @throws NegocioException si el reporte o el administrador no existen, si el nuevo estado es
     *                          PENDIENTE o nulo, si el estado es ACEPTADO o RECHAZADO sin respuesta,
     *                          o si el reporte ya estaba ACEPTADO o RECHAZADO
     */
    ReporteProducto cambiarEstado(Long idReporte, EstadoReporte nuevoEstado, String respuesta,
                                  String emailAdministrador) throws NegocioException;
}
