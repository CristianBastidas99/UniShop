package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Lógica de negocio de los productos: publicar, modificar, eliminar, descuentos y consultas.
 */
public interface ProductoServicio {

    /**
     * Publica un producto nuevo y lo asocia a las categorías indicadas.
     * El descuento nulo se guarda como 0.0.
     *
     * @param producto      producto a publicar (con vendedor, ciudad e imágenes)
     * @param idsCategorias ids de las categorías (al menos una, sin repetidos)
     * @return el producto guardado
     * @throws NegocioException si el producto ya tiene id, si falta el vendedor o la ciudad o no existen,
     *                          si no hay imágenes ni categorías, si una categoría no existe, si la fecha límite
     *                          no es futura, si las unidades o el precio no son mayores que cero o si el
     *                          descuento no está en [0, 1)
     */
    Producto publicarProducto(Producto producto, List<Long> idsCategorias) throws NegocioException;

    /**
     * Actualiza un producto existente. Solo lo puede hacer su vendedor, que no se puede cambiar.
     * Si cambia el precio o el descuento se registra un {@code HistorialPrecio}.
     *
     * @param producto      datos nuevos (el id identifica el producto)
     * @param idsCategorias nuevas categorías; si es {@code null} no se cambian
     * @param idUsuario     id del usuario que modifica
     * @return el producto actualizado
     * @throws NegocioException si el producto no existe, si el usuario no es el vendedor, si la ciudad no
     *                          existe, si no hay imágenes, si una categoría no existe o está repetida, si las
     *                          unidades son negativas, si el precio no es mayor que cero o si el descuento no
     *                          está en [0, 1)
     */
    Producto actualizarProducto(Producto producto, List<Long> idsCategorias, Long idUsuario) throws NegocioException;

    /**
     * Elimina un producto. Si tiene dependientes (detalles de compra, subastas, comentarios, chats o
     * reportes) NO se borra físicamente: se invalida poniendo su fecha límite en el momento actual, con lo
     * que deja de aparecer en los listados públicos. Si no tiene dependientes se quita de las categorías y
     * de los favoritos, se borra su historial de precios y se borra el producto.
     *
     * @throws NegocioException si el producto no existe o el usuario no es su vendedor
     */
    void eliminarProducto(Long idProducto, Long idUsuario) throws NegocioException;

    /**
     * Pone un descuento al producto y registra el cambio en el historial de precios.
     *
     * @throws NegocioException si el producto no existe, el usuario no es el vendedor o el descuento no
     *                          está en el intervalo (0, 1)
     */
    Producto ponerDescuento(Long idProducto, Long idUsuario, Double descuento) throws NegocioException;

    /**
     * Quita el descuento (queda en 0.0) y registra el cambio en el historial si había uno.
     *
     * @throws NegocioException si el producto no existe o el usuario no es el vendedor
     */
    Producto quitarDescuento(Long idProducto, Long idUsuario) throws NegocioException;

    /**
     * Calcula el precio final: precio x (1 - descuento), redondeado a 2 decimales. Un descuento nulo
     * cuenta como cero.
     */
    Double calcularPrecioConDescuento(Producto producto);

    /** Lista los productos vigentes (fecha límite futura y unidades mayores que cero). */
    List<Producto> listarVigentes();

    /**
     * Lista los productos vigentes de una categoría.
     *
     * @throws NegocioException si la categoría no existe
     */
    List<Producto> listarVigentesPorCategoria(Long idCategoria) throws NegocioException;

    /**
     * Busca productos vigentes. Todos los filtros son opcionales; el texto vacío o de solo espacios se
     * ignora y el texto se recorta.
     *
     * @throws NegocioException si algún precio es negativo, si el mínimo es mayor que el máximo o si la
     *                          calificación mínima no está entre 0 y 5
     */
    List<Producto> buscar(String texto, Long idCategoria, Double precioMin, Double precioMax, Long idCiudad,
                          Double calificacionMin) throws NegocioException;

    /**
     * Lista los productos de un vendedor, incluidos los vencidos (para "mis productos").
     *
     * @throws NegocioException si el vendedor no existe
     */
    List<Producto> listarPorVendedor(Long idUsuario) throws NegocioException;

    /** Lista los productos favoritos de un usuario (por su email). */
    List<Producto> listarFavoritos(String email);

    /**
     * Obtiene el producto por su id.
     *
     * @throws NegocioException si el id es nulo o el producto no existe
     */
    Producto obtenerProducto(Long id) throws NegocioException;

    /** Lista todos los productos, vigentes o no (uso del administrador). */
    List<Producto> listarTodos();
}
