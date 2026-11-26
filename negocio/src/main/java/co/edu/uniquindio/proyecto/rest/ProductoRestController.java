package co.edu.uniquindio.proyecto.rest;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.rest.dto.CalificacionDTO;
import co.edu.uniquindio.proyecto.rest.dto.DescuentoDTO;
import co.edu.uniquindio.proyecto.rest.dto.ProductoDTO;
import co.edu.uniquindio.proyecto.rest.dto.ProductoEntradaDTO;
import co.edu.uniquindio.proyecto.servicios.CategoriaServicio;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API REST de productos ({@code /api/productos}).
 * <p>
 * La API no tiene autenticación: el usuario que realiza una operación de escritura (modificar, eliminar,
 * cambiar descuento) se indica con el parámetro de consulta {@code idUsuario}, y el servicio valida que sea
 * el vendedor del producto. Los controladores no atrapan excepciones: las {@link NegocioException} salen y las
 * convierte el manejador global de errores (404 si no existe el recurso, 400 en datos inválidos).
 * Las respuestas son siempre DTO, nunca entidades. Se apoya en que open-in-view está activo (valor por
 * defecto de Spring Boot) para leer imágenes y categorías al mapear.
 */
@RestController
@RequestMapping("api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private ComentarioServicio comentarioServicio;

    @Autowired
    private CategoriaServicio categoriaServicio;

    @Autowired
    private ProductoMapper mapper;

    /**
     * Lista los productos vigentes (fecha límite futura y con unidades): es lo público; los vencidos
     * no se exponen.
     *
     * @return 200 con la lista de productos
     */
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        return ResponseEntity.ok(aDTOs(productoServicio.listarVigentes()));
    }

    /**
     * Busca productos vigentes; todos los filtros son opcionales.
     *
     * @param texto           texto a buscar en el nombre o la descripción
     * @param categoria       id de la categoría
     * @param precioMin       precio mínimo
     * @param precioMax       precio máximo
     * @param ciudad          id de la ciudad
     * @param calificacionMin calificación promedio mínima (0 a 5)
     * @return 200 con la lista de productos; 400 si algún filtro es inválido
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO>> buscar(@RequestParam(required = false) String texto,
                                                    @RequestParam(required = false) Long categoria,
                                                    @RequestParam(required = false) Double precioMin,
                                                    @RequestParam(required = false) Double precioMax,
                                                    @RequestParam(required = false) Long ciudad,
                                                    @RequestParam(required = false) Double calificacionMin)
            throws NegocioException {
        return ResponseEntity.ok(aDTOs(productoServicio.buscar(texto, categoria, precioMin, precioMax, ciudad,
                calificacionMin)));
    }

    /**
     * Obtiene un producto por su id.
     *
     * @param id id del producto
     * @return 200 con el producto; 404 si no existe o su vendedor está desactivado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerProducto(@PathVariable Long id) throws NegocioException {
        Producto producto = productoServicio.obtenerProductoVisible(id);
        return ResponseEntity.ok(mapper.aDTO(producto, comentarioServicio.calcularCalificacion(id)));
    }

    /**
     * Publica un producto nuevo (el vendedor va en {@code idVendedor} del cuerpo).
     *
     * @param entrada datos del producto
     * @return 201 con el producto creado y el header Location; 400 si los datos son inválidos
     */
    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody ProductoEntradaDTO entrada)
            throws NegocioException {
        Producto creado = productoServicio.publicarProducto(mapper.aEntidad(entrada), entrada.getIdsCategorias());
        ProductoDTO dto = mapper.aDTO(creado, 0.0, cargarCategorias(entrada.getIdsCategorias()));
        return ResponseEntity.created(URI.create("/api/productos/" + creado.getId())).body(dto);
    }

    /**
     * Actualiza un producto; solo puede hacerlo su vendedor. {@code idVendedor} del cuerpo se ignora.
     *
     * @param id        id del producto
     * @param idUsuario id del usuario que modifica
     * @param entrada   datos nuevos
     * @return 200 con el producto actualizado; 404 si no existe; 400 si el usuario no es el vendedor o los
     * datos son inválidos
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable Long id, @RequestParam Long idUsuario,
                                                          @Valid @RequestBody ProductoEntradaDTO entrada)
            throws NegocioException {
        Producto producto = mapper.aEntidad(entrada);
        producto.setId(id);
        Producto actualizado = productoServicio.actualizarProducto(producto, entrada.getIdsCategorias(), idUsuario);
        ProductoDTO dto = mapper.aDTO(actualizado, comentarioServicio.calcularCalificacion(id),
                cargarCategorias(entrada.getIdsCategorias()));
        return ResponseEntity.ok(dto);
    }

    /**
     * Elimina un producto. Si tiene dependientes (compras, subastas, etc.) se invalida (deja de ser vigente)
     * en lugar de borrarse.
     *
     * @param id        id del producto
     * @param idUsuario id del usuario que elimina (debe ser el vendedor)
     * @return 204 sin cuerpo; 404 si no existe; 400 si el usuario no es el vendedor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id, @RequestParam Long idUsuario)
            throws NegocioException {
        productoServicio.eliminarProducto(id, idUsuario);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene la calificación promedio de un producto y su número de comentarios.
     *
     * @param id id del producto
     * @return 200 con {@code idProducto}, {@code promedio} y {@code cantidadComentarios}; 404 si no existe
     */
    @GetMapping("/{id}/calificacion")
    public ResponseEntity<CalificacionDTO> obtenerCalificacion(@PathVariable Long id) throws NegocioException {
        double promedio = comentarioServicio.calcularCalificacion(id);
        int cantidad = comentarioServicio.listarComentarioProducto(id).size();
        return ResponseEntity.ok(new CalificacionDTO(id, promedio, cantidad));
    }

    /**
     * Pone o quita el descuento de un producto (queda registrado en el historial de precios).
     * Un descuento nulo o 0 lo quita; si no, debe estar en el intervalo (0, 1).
     *
     * @param id        id del producto
     * @param idUsuario id del usuario que actúa (debe ser el vendedor)
     * @param cuerpo    {@code { "descuento": 0.2 }}
     * @return 200 con el producto actualizado; 404 si no existe; 400 si el descuento es inválido o el usuario
     * no es el vendedor
     */
    @PatchMapping("/{id}/descuento")
    public ResponseEntity<ProductoDTO> cambiarDescuento(@PathVariable Long id, @RequestParam Long idUsuario,
                                                        @RequestBody DescuentoDTO cuerpo) throws NegocioException {
        Double descuento = cuerpo.getDescuento();
        Producto producto = (descuento == null || descuento == 0.0)
                ? productoServicio.quitarDescuento(id, idUsuario)
                : productoServicio.ponerDescuento(id, idUsuario, descuento);
        return ResponseEntity.ok(mapper.aDTO(producto, comentarioServicio.calcularCalificacion(id)));
    }

    /** Convierte una lista de productos a DTO calculando las calificaciones en una sola consulta. */
    private List<ProductoDTO> aDTOs(List<Producto> productos) {
        List<Long> ids = new ArrayList<>();
        for (Producto p : productos) {
            ids.add(p.getId());
        }
        Map<Long, Double> calificaciones = comentarioServicio.calcularCalificaciones(ids);
        List<ProductoDTO> resultado = new ArrayList<>();
        for (Producto p : productos) {
            resultado.add(mapper.aDTO(p, calificaciones.get(p.getId())));
        }
        return resultado;
    }

    /** Carga las categorías por id (ya validadas por el servicio al guardar). */
    private List<Categoria> cargarCategorias(List<Long> ids) throws NegocioException {
        List<Categoria> categorias = new ArrayList<>();
        for (Long id : ids) {
            categorias.add(categoriaServicio.obtenerCategoria(id));
        }
        return categorias;
    }
}
