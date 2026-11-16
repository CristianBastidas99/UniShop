package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones básicas (CRUD) sobre subastas. Las reglas de crear, pujar y cerrar subastas se agregan después.
 */
public interface SubastaServicio {

    /**
     * Registra una subasta nueva.
     *
     * @throws NegocioException si el id viene informado y ya existe
     */
    Subasta registrarSubasta(Subasta s) throws NegocioException;

    /**
     * Actualiza una subasta.
     *
     * @throws NegocioException si el id es nulo o la subasta no existe
     */
    Subasta actualizarSubasta(Subasta s) throws NegocioException;

    /**
     * Obtiene una subasta por su id.
     *
     * @throws NegocioException si el id es nulo o la subasta no existe
     */
    Subasta obtenerSubasta(Long id) throws NegocioException;

    /**
     * Elimina una subasta por su id.
     *
     * @throws NegocioException si el id es nulo, la subasta no existe o ya tiene pujas
     */
    void eliminarSubasta(Long id) throws NegocioException;

    /** Lista todas las subastas. */
    List<Subasta> listarSubastas();

    /** Lista las subastas activas (sin cerrar y con fecha límite posterior a la fecha actual). */
    List<Subasta> listarActivas();

    /** Lista las subastas cuya fecha límite ya pasó y que todavía no se han cerrado. */
    List<Subasta> listarVencidasSinCerrar();

    /**
     * Crea una subasta para un producto del vendedor. La fecha límite es la fecha actual más las horas indicadas.
     *
     * @param idProducto producto a subastar
     * @param idVendedor usuario que subasta (debe ser el dueño del producto)
     * @param horas      duración en horas (al menos 1)
     * @throws NegocioException si el producto no existe, no es del vendedor, no está vigente o no tiene unidades,
     *                          si la duración no es válida, si la subasta terminaría después de la fecha límite
     *                          del producto o si el producto ya tiene una subasta activa
     */
    Subasta crearSubasta(Long idProducto, Long idVendedor, int horas) throws NegocioException;

    /**
     * Cierra una subasta vencida. Si tiene pujas, la más alta gana y se genera su compra (1 unidad al valor de la
     * puja) enlazada a la subasta. Si no hay pujas, o el producto ya no tiene unidades, se cierra sin compra.
     *
     * @throws NegocioException si la subasta no existe, ya está cerrada o todavía está activa
     */
    Subasta cerrarSubasta(Long idSubasta) throws NegocioException;
}
