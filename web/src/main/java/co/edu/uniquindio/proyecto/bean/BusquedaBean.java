package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CategoriaServicio;
import co.edu.uniquindio.proyecto.servicios.CiudadServicio;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean de la página de resultados. Los filtros llegan por la URL (GET, se pueden guardar o recargar):
 * {@code resultadoBusqueda.xhtml?q=&categoria=&precioMin=&precioMax=&ciudad=&calificacion=}.
 * Los parámetros se reciben como texto y se convierten en {@link #cargar()}, para que un valor
 * no válido se ignore con un aviso en vez de provocar un error.
 */
@Component
@ViewScoped
public class BusquedaBean extends ListadoProductosBase {

    // Parámetros de la URL y, a la vez, valores del formulario de filtros
    @Getter @Setter private String q;
    @Getter @Setter private String categoria;
    @Getter @Setter private String ciudad;
    @Getter @Setter private String calificacion;
    @Getter @Setter private String precioMin;
    @Getter @Setter private String precioMax;

    // Precios del formulario (numéricos, validados en la vista)
    @Getter @Setter private Double formPrecioMin;
    @Getter @Setter private Double formPrecioMax;

    /** Texto que se buscó realmente (el campo q puede cambiar al editar el formulario). */
    @Getter private String textoBuscado = "";

    @Getter private List<Producto> productos = new ArrayList<>();

    private final CategoriaServicio categoriaServicio;
    private final CiudadServicio ciudadServicio;

    public BusquedaBean(ProductoServicio productoServicio, ComentarioServicio comentarioServicio,
                        CategoriaServicio categoriaServicio, CiudadServicio ciudadServicio) {
        super(productoServicio, comentarioServicio);
        this.categoriaServicio = categoriaServicio;
        this.ciudadServicio = ciudadServicio;
    }

    /** Convierte los filtros, consulta el servicio y carga las calificaciones (f:viewAction). */
    public void cargar() {
        textoBuscado = q == null ? "" : q.trim();
        Long idCategoria = convertirId(categoria, "categoría");
        Long idCiudad = convertirId(ciudad, "ciudad");
        // Un id numérico que no existe también se ignora con aviso (si no, el menú fallaría al enviarlo)
        try {
            if (idCategoria != null) categoriaServicio.obtenerCategoria(idCategoria);
        } catch (NegocioException e) {
            avisarFiltroInvalido("categoría");
            idCategoria = null;
        }
        try {
            if (idCiudad != null) ciudadServicio.obtenerCiudad(idCiudad);
        } catch (NegocioException e) {
            avisarFiltroInvalido("ciudad");
            idCiudad = null;
        }
        Double minimo = convertirNumero(precioMin, "precio mínimo");
        Double maximo = convertirNumero(precioMax, "precio máximo");
        Double minimaCalificacion = convertirCalificacion(calificacion);

        // Un filtro inválido se descarta también del formulario (si no, el menú fallaría al enviarlo)
        if (idCategoria == null) categoria = null;
        if (idCiudad == null) ciudad = null;
        if (minimaCalificacion == null) calificacion = null;
        formPrecioMin = minimo;
        formPrecioMax = maximo;

        try {
            productos = productoServicio.buscar(textoBuscado, idCategoria, minimo, maximo, idCiudad, minimaCalificacion);
        } catch (NegocioException e) {
            productos = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Búsqueda no válida", e.getMessage()));
        }
        cargarCalificaciones(productos);
    }

    /** Redirige por GET con los filtros que tengan valor. */
    public String aplicar() {
        StringBuilder url = new StringBuilder("/resultadoBusqueda?faces-redirect=true");
        agregar(url, "q", q == null ? null : q.trim());
        agregar(url, "categoria", categoria);
        agregar(url, "precioMin", formPrecioMin == null ? null : BigDecimal.valueOf(formPrecioMin).stripTrailingZeros().toPlainString());
        agregar(url, "precioMax", formPrecioMax == null ? null : BigDecimal.valueOf(formPrecioMax).stripTrailingZeros().toPlainString());
        agregar(url, "ciudad", ciudad);
        agregar(url, "calificacion", calificacion);
        return url.toString();
    }

    public String getTitulo() {
        return textoBuscado.isEmpty() ? "Resultados de la búsqueda" : "Resultados para \"" + textoBuscado + "\"";
    }

    public String getCantidad() {
        int total = productos.size();
        return total + (total == 1 ? " producto encontrado" : " productos encontrados");
    }

    private void agregar(StringBuilder url, String nombre, String valor) {
        if (valor != null && !valor.trim().isEmpty()) {
            url.append('&').append(nombre).append('=').append(BuscadorBean.codificar(valor.trim()));
        }
    }

    private Long convertirId(String valor, String nombre) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            avisarFiltroInvalido(nombre);
            return null;
        }
    }

    private Double convertirNumero(String valor, String nombre) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            double numero = Double.parseDouble(valor.trim());
            if (Double.isNaN(numero) || Double.isInfinite(numero)) {
                throw new NumberFormatException();
            }
            return numero;
        } catch (NumberFormatException e) {
            avisarFiltroInvalido(nombre);
            return null;
        }
    }

    /** La calificación del menú son enteros del 1 al 5; el rango lo valida el servicio. */
    private Double convertirCalificacion(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return (double) Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            avisarFiltroInvalido("calificación");
            return null;
        }
    }

    private void avisarFiltroInvalido(String nombre) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Filtro no válido", "El filtro " + nombre + " no es válido"));
    }
}
