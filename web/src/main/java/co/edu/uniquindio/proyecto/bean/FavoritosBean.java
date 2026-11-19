package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Bean de {@code usuario/favoritos.xhtml}: lista los productos favoritos del usuario en sesión (incluso los
 * vencidos, para poder quitarlos) y permite quitarlos. Es ViewScoped: tras cada acción se recarga desde el servicio.
 */
@Component
@ViewScoped
public class FavoritosBean extends ListadoProductosBase {

    private final SeguridadBean seguridadBean;

    private List<Producto> productos = new ArrayList<>();

    public FavoritosBean(ProductoServicio productoServicio, ComentarioServicio comentarioServicio,
                         SeguridadBean seguridadBean) {
        super(productoServicio, comentarioServicio);
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        try {
            productos = productoServicio.listarFavoritos(seguridadBean.getIdUsuario());
        } catch (NegocioException e) {
            productos = new ArrayList<>();
            aviso(e.getMessage());
        }
        // Las entidades quedan separadas entre peticiones: se inicializan las imágenes que usa la tarjeta
        productos.forEach(p -> {
            if (p.getMisImagenes() != null) {
                p.getMisImagenes().size();
            }
        });
        cargarCalificaciones(productos);
    }

    public void quitar(Long idProducto) {
        try {
            productoServicio.quitarFavorito(idProducto, seguridadBean.getIdUsuario());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Listo", "Quitado de favoritos"));
        } catch (NegocioException e) {
            aviso(e.getMessage());
        }
        cargar();
    }

    /** Vencido o sin unidades. */
    public boolean noDisponible(Producto producto) {
        return producto.getUnidades() <= 0 || producto.getFechaLimite() == null
                || !producto.getFechaLimite().after(new Date());
    }

    private static void aviso(String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", texto));
    }

    public List<Producto> getProductos() {
        return productos;
    }
}
