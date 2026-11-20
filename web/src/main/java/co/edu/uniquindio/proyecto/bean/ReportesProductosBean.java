package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.entidades.MotivoReporte;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ReporteProductoServicio;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Bean de {@code admin/reportes-productos.xhtml}: el administrador ve los reportes por revisar y los resueltos,
 * cambia el estado y deja una respuesta (un reporte aceptado invalida el producto).
 */
@Component
@ViewScoped
public class ReportesProductosBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ReportesProductosBean.class);

    public static final int MAX_RESPUESTA = 1000;

    /** Reporte ya copiado a datos simples. */
    public static class Fila implements Serializable {
        private final Long id;
        private final Date fecha;
        private final Long idProducto;
        private final String producto;
        private final String reportadoPor;
        private final String motivo;
        private final String descripcion;
        private final EstadoReporte estado;
        private final String respuesta;
        private final String administrador;

        Fila(Long id, Date fecha, Long idProducto, String producto, String reportadoPor, String motivo,
             String descripcion, EstadoReporte estado, String respuesta, String administrador) {
            this.id = id;
            this.fecha = fecha;
            this.idProducto = idProducto;
            this.producto = producto;
            this.reportadoPor = reportadoPor;
            this.motivo = motivo;
            this.descripcion = descripcion;
            this.estado = estado;
            this.respuesta = respuesta;
            this.administrador = administrador;
        }

        public Long getId() { return id; }
        public Date getFecha() { return fecha; }
        public Long getIdProducto() { return idProducto; }
        public String getProducto() { return producto; }
        public String getReportadoPor() { return reportadoPor; }
        public String getMotivo() { return motivo; }
        public String getDescripcion() { return descripcion; }
        public EstadoReporte getEstado() { return estado; }
        public String getEstadoTexto() { return etiquetaEstado(estado); }
        public String getClaseEstado() {
            switch (estado) {
                case ACEPTADO: return "estado-vigente";
                case RECHAZADO: return "estado-agotado";
                case EN_REVISION: return "estado-proceso";
                default: return "estado-inactivo";
            }
        }
        public String getRespuesta() { return respuesta; }
        public String getAdministrador() { return administrador; }
    }

    private final ReporteProductoServicio reporteServicio;
    private final SeguridadBean seguridadBean;

    private List<Fila> porRevisar = new ArrayList<>();
    private List<Fila> resueltos = new ArrayList<>();

    // Reporte en revisión (diálogo)
    private Fila seleccionado;
    private String nuevoEstado;
    private String respuesta;

    public ReportesProductosBean(ReporteProductoServicio reporteServicio, SeguridadBean seguridadBean) {
        this.reporteServicio = reporteServicio;
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        recargar();
    }

    private void recargar() {
        porRevisar = new ArrayList<>();
        resueltos = new ArrayList<>();
        try {
            for (ReporteProducto r : reporteServicio.listarPorRevisar()) {
                porRevisar.add(copiar(r));
            }
            for (ReporteProducto r : reporteServicio.listarResueltos()) {
                resueltos.add(copiar(r));
            }
        } catch (Exception e) {
            errorInesperado("cargar los reportes", e);
        }
    }

    private Fila copiar(ReporteProducto r) {
        return new Fila(r.getId(), r.getFecha(),
                r.getProducto() == null ? null : r.getProducto().getId(),
                r.getProducto() == null ? "" : r.getProducto().getNombre(),
                r.getUsuario() == null ? "" : r.getUsuario().getNombre(),
                etiquetaMotivo(r.getMotivo()), r.getDescripcion(), r.getEstado(),
                r.getRespuestaAdministrador(),
                r.getAdministrador() == null ? "" : r.getAdministrador().getNombre());
    }

    static String etiquetaMotivo(MotivoReporte motivo) {
        if (motivo == null) {
            return "";
        }
        switch (motivo) {
            case FRAUDE: return "Fraude";
            case PRODUCTO_PROHIBIDO: return "Producto prohibido";
            case INFORMACION_FALSA: return "Información falsa";
            case CONTENIDO_OFENSIVO: return "Contenido ofensivo";
            default: return "Otro";
        }
    }

    static String etiquetaEstado(EstadoReporte estado) {
        switch (estado) {
            case PENDIENTE: return "Pendiente";
            case EN_REVISION: return "En revisión";
            case ACEPTADO: return "Aceptado";
            default: return "Rechazado";
        }
    }

    // ---------- Revisar ----------

    public void prepararRevision(Fila fila) {
        seleccionado = fila;
        nuevoEstado = fila.getEstado() == EstadoReporte.EN_REVISION ? "EN_REVISION" : null;
        respuesta = fila.getRespuesta();
    }

    /** Guarda el nuevo estado y la respuesta. El diálogo solo se cierra (parámetro "ok") si salió bien. */
    public void guardar() {
        if (seleccionado == null) {
            aviso(FacesMessage.SEVERITY_WARN, "Elige un reporte para revisar");
            return;
        }
        if (nuevoEstado == null || nuevoEstado.isEmpty()) {
            aviso(FacesMessage.SEVERITY_WARN, "Elige el nuevo estado del reporte");
            return;
        }
        try {
            EstadoReporte estado = EstadoReporte.valueOf(nuevoEstado);
            reporteServicio.cambiarEstado(seleccionado.getId(), estado,
                    respuesta == null ? null : respuesta.trim(), seguridadBean.getEmailAdministrador());
            String extra = estado == EstadoReporte.ACEPTADO
                    ? ". El producto " + seleccionado.getProducto() + " quedó invalidado" : "";
            aviso(FacesMessage.SEVERITY_INFO, "Reporte " + etiquetaEstado(estado).toLowerCase() + extra);
            PrimeFaces.current().ajax().addCallbackParam("ok", true);
            recargar();
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("guardar la revisión del reporte", e);
        }
    }

    private void errorInesperado(String accion, Exception e) {
        LOG.error("Error inesperado al {}", accion, e);
        aviso(FacesMessage.SEVERITY_ERROR, "No se pudo " + accion + ". Inténtalo de nuevo más tarde.");
    }

    private void aviso(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severidad, severidad == FacesMessage.SEVERITY_INFO ? "Listo" : "Atención", texto));
    }

    public List<Fila> getPorRevisar() { return porRevisar; }
    public List<Fila> getResueltos() { return resueltos; }
    public Fila getSeleccionado() { return seleccionado; }
    public String getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(String nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    public int getMaxRespuesta() { return MAX_RESPUESTA; }
}
