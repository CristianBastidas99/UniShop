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
import java.util.ArrayList;
import java.util.List;

/**
 * Bean de la página de inicio: lista los productos vigentes, opcionalmente filtrados por categoría
 * ({@code index.xhtml?categoria=ID}), con su precio final y su calificación.
 */
@Component
@ViewScoped
public class InicioBean extends ListadoProductosBase {

    private final CategoriaServicio categoriaServicio;

    /** Valor crudo del parámetro {@code categoria} (texto, para poder tolerar valores no numéricos). */
    private String idCategoria;

    /** Id de la categoría válida seleccionada, o null si se muestran todos los productos. */
    private Long categoriaSeleccionada;
    private String nombreCategoria;

    private List<Producto> productos = new ArrayList<>();

    public InicioBean(ProductoServicio productoServicio, ComentarioServicio comentarioServicio,
                      CategoriaServicio categoriaServicio) {
        super(productoServicio, comentarioServicio);
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
        cargarCalificaciones(productos);
    }

    /** Título de la página según la categoría seleccionada. */
    public String getTitulo() {
        return nombreCategoria == null ? "Todos los productos" : "Productos de " + nombreCategoria;
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
