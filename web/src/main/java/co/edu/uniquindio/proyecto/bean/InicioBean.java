package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CategoriaServicio;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean de la página de inicio: lista los productos vigentes, opcionalmente filtrados por categoría
 * ({@code index.xhtml?categoria=ID}), con su precio final y su calificación.
 */
@Component
@ViewScoped
public class InicioBean implements Serializable {

    /** Ruta (desde la raíz de la aplicación) de la imagen que se muestra si el producto no tiene imágenes. */
    private static final String IMAGEN_POR_DEFECTO = "resources/img/sin-imagen.svg";

    private final ProductoServicio productoServicio;
    private final ComentarioServicio comentarioServicio;
    private final CategoriaServicio categoriaServicio;

    /** Valor crudo del parámetro {@code categoria} (texto, para poder tolerar valores no numéricos). */
    private String idCategoria;

    /** Id de la categoría válida seleccionada, o null si se muestran todos los productos. */
    private Long categoriaSeleccionada;
    private String nombreCategoria;

    private List<Producto> productos = new ArrayList<>();
    private Map<Long, Double> calificaciones = new HashMap<>();

    public InicioBean(ProductoServicio productoServicio, ComentarioServicio comentarioServicio,
                      CategoriaServicio categoriaServicio) {
        this.productoServicio = productoServicio;
        this.comentarioServicio = comentarioServicio;
        this.categoriaServicio = categoriaServicio;
    }

    /**
     * Carga los productos (se ejecuta con f:viewAction al abrir la página). Si la categoría pedida no es
     * válida avisa con un mensaje y lista todos los productos.
     */
    public void cargar() {
        categoriaSeleccionada = null;
        nombreCategoria = null;
        List<Producto> encontrados = null;

        if (idCategoria != null && !idCategoria.trim().isEmpty()) {
            try {
                Long id = Long.valueOf(idCategoria.trim());
                encontrados = productoServicio.listarVigentesPorCategoria(id);
                categoriaSeleccionada = id;
                nombreCategoria = categoriaServicio.obtenerCategoria(id).getNombre();
            } catch (NumberFormatException | NegocioException e) {
                // Id no numérico o categoría inexistente: se avisa y se muestra todo
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Categoría no encontrada", "La categoría no existe"));
                encontrados = null;
            }
        }
        productos = encontrados != null ? encontrados : productoServicio.listarVigentes();

        // Una sola consulta para las calificaciones de todos los productos listados
        List<Long> ids = productos.stream().map(Producto::getId).collect(Collectors.toList());
        calificaciones = comentarioServicio.calcularCalificaciones(ids);
    }

    /** Título de la página según la categoría seleccionada. */
    public String getTitulo() {
        return nombreCategoria == null ? "Todos los productos" : "Productos de " + nombreCategoria;
    }

    /** Calificación promedio (1 decimal) del producto; 0 si no tiene comentarios. */
    public Double calificacion(Producto producto) {
        return calificaciones.getOrDefault(producto.getId(), 0.0);
    }

    /** Indica si el producto tiene calificación (0 se interpreta como "sin calificaciones"). */
    public boolean tieneCalificacion(Producto producto) {
        return calificacion(producto) > 0;
    }

    /** Calificación redondeada a estrellas enteras para p:rating. */
    public Integer estrellas(Producto producto) {
        return (int) Math.round(calificacion(producto));
    }

    public boolean tieneDescuento(Producto producto) {
        return producto.getDescuento() != null && producto.getDescuento() > 0;
    }

    /** Precio con el descuento aplicado. */
    public Double precioFinal(Producto producto) {
        return productoServicio.calcularPrecioConDescuento(producto);
    }

    /** Porcentaje de descuento como entero (por ejemplo, 25). */
    public int porcentajeDescuento(Producto producto) {
        return producto.getDescuento() == null ? 0 : (int) Math.round(producto.getDescuento() * 100);
    }

    /** Ruta de la primera imagen del producto, o de la imagen por defecto si no tiene. */
    public String imagen(Producto producto) {
        String contexto = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        String ruta = producto.getMisImagenes() == null || producto.getMisImagenes().isEmpty()
                ? IMAGEN_POR_DEFECTO : producto.getMisImagenes().get(0);
        return contexto + "/" + ruta;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Long getCategoriaSeleccionada() {
        return categoriaSeleccionada;
    }

    public List<Producto> getProductos() {
        return productos;
    }
}
