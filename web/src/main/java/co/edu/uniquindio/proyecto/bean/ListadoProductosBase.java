package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base de los beans que muestran productos con la tarjeta reutilizable
 * ({@code WEB-INF/includes/tarjetaProducto.xhtml}): calificaciones, precios con descuento e imagen.
 */
public abstract class ListadoProductosBase implements Serializable {

    /** Ruta (desde la raíz de la aplicación) de la imagen que se muestra si el producto no tiene imágenes. */
    private static final String IMAGEN_POR_DEFECTO = "resources/img/sin-imagen.svg";

    protected final ProductoServicio productoServicio;
    private final ComentarioServicio comentarioServicio;

    private Map<Long, Double> calificaciones = new HashMap<>();

    protected ListadoProductosBase(ProductoServicio productoServicio, ComentarioServicio comentarioServicio) {
        this.productoServicio = productoServicio;
        this.comentarioServicio = comentarioServicio;
    }

    /** Carga, con una sola consulta, las calificaciones de todos los productos de la lista. */
    protected void cargarCalificaciones(List<Producto> productos) {
        List<Long> ids = productos.stream().map(Producto::getId).collect(Collectors.toList());
        calificaciones = comentarioServicio.calcularCalificaciones(ids);
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
}
