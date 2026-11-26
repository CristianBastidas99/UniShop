package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaUsuarioServicio;
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
 * Bean de {@code subasta.xhtml?subasta=ID}: datos de la subasta, puja más alta, lista de pujas y formulario
 * para pujar. Copia todo a datos simples (la vista no depende de la sesión de Hibernate) y no muestra emails.
 */
@Component
@ViewScoped
public class SubastaBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SubastaBean.class);
    private static final String IMAGEN_POR_DEFECTO = "resources/img/sin-imagen.svg";

    /** Puja copiada a datos simples. */
    public static class Puja implements Serializable {
        private final String usuario;
        private final double valor;
        private final Date fecha;

        Puja(String usuario, double valor, Date fecha) {
            this.usuario = usuario;
            this.valor = valor;
            this.fecha = fecha;
        }

        public String getUsuario() { return usuario; }
        public double getValor() { return valor; }
        public Date getFecha() { return fecha; }
    }

    private final SubastaServicio subastaServicio;
    private final SubastaUsuarioServicio pujaServicio;
    private final SeguridadBean seguridadBean;

    /** Valor crudo del parámetro {@code subasta} (texto, para tolerar valores no numéricos). */
    private String idSubasta;

    private Long id;
    private boolean existe;
    private boolean cerrada;
    private boolean activa;
    private Date fechaFin;
    private long restanteMs;
    private Long idProducto;
    private String nombreProducto;
    private String descripcion;
    private String imagen;
    private double precioProducto;
    private String vendedor;
    private Long idVendedor;
    private boolean conCompra;

    private List<Puja> pujas = new ArrayList<>();
    private double pujaMasAlta;

    /** Monto que escribe el usuario. */
    private Double monto;

    public SubastaBean(SubastaServicio subastaServicio, SubastaUsuarioServicio pujaServicio,
                       SeguridadBean seguridadBean) {
        this.subastaServicio = subastaServicio;
        this.pujaServicio = pujaServicio;
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction: carga la subasta o marca que no existe (sin lanzar error). */
    public void cargar() {
        existe = false;
        Long idNumerico;
        try {
            idNumerico = Long.valueOf(idSubasta == null ? "" : idSubasta.trim());
        } catch (NumberFormatException e) {
            return;
        }
        try {
            recargar(idNumerico);
            existe = true;
        } catch (NegocioException e) {
            existe = false;
        }
    }

    private void recargar(Long idNumerico) throws NegocioException {
        Subasta s = subastaServicio.obtenerSubasta(idNumerico);
        if (!s.getProducto().getUsuario().isActivo()) {
            // El vendedor está desactivado: la subasta se trata como inexistente
            throw new NegocioException("La subasta no existe");
        }
        id = s.getId();
        Producto p = s.getProducto();
        idProducto = p.getId();
        nombreProducto = p.getNombre();
        descripcion = p.getDescripcion();
        precioProducto = p.getPrecio();
        String contexto = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        imagen = contexto + "/" + (p.getMisImagenes() == null || p.getMisImagenes().isEmpty()
                ? IMAGEN_POR_DEFECTO : p.getMisImagenes().get(0));
        vendedor = p.getUsuario().getNombre();
        idVendedor = p.getUsuario().getId();
        fechaFin = s.getFechaLimite();
        cerrada = s.isCerrada();
        conCompra = s.getCompra() != null;
        long restante = fechaFin.getTime() - System.currentTimeMillis();
        activa = !cerrada && restante > 0;
        restanteMs = Math.max(0, restante);
        recargarPujas();
    }

    private void recargarPujas() throws NegocioException {
        pujas = new ArrayList<>();
        for (SubastaUsuario su : pujaServicio.listarPorSubasta(id)) {
            pujas.add(new Puja(su.getUsuario().getNombre(), su.getValor(), su.getFechaSubasta()));
        }
        pujaMasAlta = pujas.isEmpty() ? 0 : pujas.get(0).getValor();
    }

    /** Puja del usuario en sesión (solo si es un usuario y no es el vendedor). */
    public void pujar() {
        if (!isPuedePujar()) {
            aviso(FacesMessage.SEVERITY_WARN, "No puedes pujar en esta subasta");
            return;
        }
        if (monto == null) {
            aviso(FacesMessage.SEVERITY_WARN, "Escribe el monto de tu puja");
            return;
        }
        try {
            pujaServicio.pujar(id, seguridadBean.getIdUsuario(), monto);
            aviso(FacesMessage.SEVERITY_INFO, "Puja registrada por $" + String.format("%,d", Math.round(monto)));
            monto = null;
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            LOG.error("Error inesperado al pujar", e);
            aviso(FacesMessage.SEVERITY_ERROR, "No se pudo registrar la puja. Inténtalo de nuevo más tarde.");
        }
        refrescar();
    }

    /** Refresca los datos (lo usa el p:poll). Si la subasta terminó, pide al cliente recargar la página. */
    public void refrescar() {
        boolean estabaActiva = activa;
        try {
            recargar(id);
        } catch (NegocioException e) {
            existe = false;
        }
        if (estabaActiva && !activa) {
            PrimeFaces.current().ajax().addCallbackParam("terminada", true);
        }
    }

    private static void aviso(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad,
                severidad == FacesMessage.SEVERITY_INFO ? "Listo" : "Atención", texto));
    }

    // ---------- Lógica de la vista ----------

    public boolean isPuedePujar() {
        return existe && activa && seguridadBean.isUsuario() && !idVendedor.equals(seguridadBean.getIdUsuario());
    }

    public boolean isEsVendedor() {
        return existe && seguridadBean.isUsuario() && idVendedor.equals(seguridadBean.getIdUsuario());
    }

    public boolean isTieneCompra() { return conCompra; }

    /** Monto mínimo sugerido: un peso más que la puja más alta (o 1 si no hay pujas). */
    public long getMinimoSugerido() { return (long) pujaMasAlta + 1; }

    public boolean isConPujas() { return !pujas.isEmpty(); }

    /** Nombre de quien tiene la puja más alta (el ganador si la subasta ya terminó). */
    public String getGanador() { return pujas.isEmpty() ? null : pujas.get(0).getUsuario(); }

    // ---------- Getters y setters ----------

    public String getIdSubasta() { return idSubasta; }
    public void setIdSubasta(String idSubasta) { this.idSubasta = idSubasta; }
    public Long getId() { return id; }
    public boolean isExiste() { return existe; }
    public boolean isCerrada() { return cerrada; }
    public boolean isActiva() { return activa; }
    public Date getFechaFin() { return fechaFin; }
    public long getRestanteMs() { return restanteMs; }
    public Long getIdProducto() { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public String getDescripcion() { return descripcion; }
    public String getImagen() { return imagen; }
    public double getPrecioProducto() { return precioProducto; }
    public String getVendedor() { return vendedor; }
    public List<Puja> getPujas() { return pujas; }
    public double getPujaMasAlta() { return pujaMasAlta; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
}
