package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.Date;

/**
 * Bean de {@code usuario/crearSubasta.xhtml?producto=ID}: el vendedor elige la duración (en horas) de la
 * subasta de uno de sus productos vigentes.
 */
@Component
@ViewScoped
public class CrearSubastaBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(CrearSubastaBean.class);

    private final ProductoServicio productoServicio;
    private final SubastaServicio subastaServicio;
    private final SeguridadBean seguridadBean;

    private String idProducto;
    private Long id;
    private boolean formularioVisible;
    private String nombre;
    private Date fechaLimite;
    private long maxHoras;
    private Integer horas = 24;

    public CrearSubastaBean(ProductoServicio productoServicio, SubastaServicio subastaServicio,
                            SeguridadBean seguridadBean) {
        this.productoServicio = productoServicio;
        this.subastaServicio = subastaServicio;
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction: solo muestra el formulario si el producto es del usuario y está vigente. */
    public void cargar() {
        formularioVisible = false;
        try {
            Producto p = productoServicio.obtenerProducto(Long.valueOf(idProducto == null ? "" : idProducto.trim()));
            Date ahora = new Date();
            if (!p.getUsuario().getId().equals(seguridadBean.getIdUsuario())) {
                return;
            }
            id = p.getId();
            nombre = p.getNombre();
            fechaLimite = p.getFechaLimite();
            if (fechaLimite == null || !fechaLimite.after(ahora) || p.getUnidades() <= 0) {
                aviso(FacesMessage.SEVERITY_WARN, "Solo se pueden subastar productos vigentes y con unidades");
                return;
            }
            maxHoras = Math.max(1, (fechaLimite.getTime() - ahora.getTime()) / 3_600_000L);
            if (horas != null && horas > maxHoras) {
                horas = (int) Math.min(maxHoras, Integer.MAX_VALUE);
            }
            formularioVisible = true;
        } catch (NumberFormatException | NegocioException e) {
            formularioVisible = false;
        }
    }

    /** Crea la subasta; si todo sale bien, va a la página de la subasta con un aviso "Subasta creada". */
    public String crear() {
        if (horas == null || horas < 1) {
            aviso(FacesMessage.SEVERITY_WARN, "La duración debe ser de al menos una hora");
            return null;
        }
        try {
            Long idSubasta = subastaServicio.crearSubasta(id, seguridadBean.getIdUsuario(), horas).getId();
            aviso(FacesMessage.SEVERITY_INFO, "Subasta creada");
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            return "/subasta?faces-redirect=true&subasta=" + idSubasta;
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            LOG.error("Error inesperado al crear la subasta", e);
            aviso(FacesMessage.SEVERITY_ERROR, "No se pudo crear la subasta. Inténtalo de nuevo más tarde.");
        }
        return null;
    }

    private static void aviso(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad,
                severidad == FacesMessage.SEVERITY_INFO ? "Listo" : "Atención", texto));
    }

    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }
    public boolean isFormularioVisible() { return formularioVisible; }
    public String getNombre() { return nombre; }
    public Date getFechaLimite() { return fechaLimite; }
    public long getMaxHoras() { return maxHoras; }
    public Integer getHoras() { return horas; }
    public void setHoras(Integer horas) { this.horas = horas; }
}
