package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CuponServicio;
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
 * Bean de {@code admin/cupones.xhtml}: el administrador lista, crea, edita, desactiva y elimina cupones.
 * El porcentaje se muestra y se escribe como entero (1 a 99) y se guarda como fracción (0 a 1).
 */
@Component
@ViewScoped
public class CuponesAdminBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(CuponesAdminBean.class);

    /** Cupón ya copiado a datos simples. */
    public static class Fila implements Serializable {
        private final Long id;
        private final String codigo;
        private final int porcentaje;
        private final Date fechaVencimiento;
        private final int usosRealizados;
        private final int usosMaximos;
        private final String estado;

        Fila(Long id, String codigo, int porcentaje, Date fechaVencimiento, int usosRealizados, int usosMaximos,
             String estado) {
            this.id = id;
            this.codigo = codigo;
            this.porcentaje = porcentaje;
            this.fechaVencimiento = fechaVencimiento;
            this.usosRealizados = usosRealizados;
            this.usosMaximos = usosMaximos;
            this.estado = estado;
        }

        public Long getId() { return id; }
        public String getCodigo() { return codigo; }
        public int getPorcentaje() { return porcentaje; }
        public Date getFechaVencimiento() { return fechaVencimiento; }
        public int getUsosRealizados() { return usosRealizados; }
        public int getUsosMaximos() { return usosMaximos; }
        public String getEstado() { return estado; }
        public boolean isVigente() { return "Vigente".equals(estado); }
        public String getClaseEstado() {
            switch (estado) {
                case "Vigente": return "estado-vigente";
                case "Agotado": return "estado-agotado";
                default: return "estado-inactivo";
            }
        }
    }

    private final CuponServicio cuponServicio;

    private List<Fila> cupones = new ArrayList<>();

    // Formulario de creación y edición
    private Long idEdicion;
    private String codigo;
    private Integer porcentaje;
    private Date fechaVencimiento;
    private Integer usosMaximos;

    // Cupón elegido para eliminar
    private Long idSeleccionado;
    private String codigoSeleccionado;

    public CuponesAdminBean(CuponServicio cuponServicio) {
        this.cuponServicio = cuponServicio;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        recargar();
    }

    private void recargar() {
        cupones = new ArrayList<>();
        try {
            Date ahora = new Date();
            for (Cupon c : cuponServicio.listarCupones()) {
                String estado;
                if (c.getFechaVencimiento() == null || !c.getFechaVencimiento().after(ahora)) {
                    estado = "Vencido";
                } else if (c.getUsosRealizados() >= c.getUsosMaximos()) {
                    estado = "Agotado";
                } else {
                    estado = "Vigente";
                }
                int pct = c.getPorcentaje() == null ? 0 : (int) Math.round(c.getPorcentaje() * 100);
                cupones.add(new Fila(c.getId(), c.getCodigo(), pct, c.getFechaVencimiento(),
                        c.getUsosRealizados(), c.getUsosMaximos(), estado));
            }
        } catch (Exception e) {
            errorInesperado("cargar los cupones", e);
        }
    }

    // ---------- Crear y editar ----------

    public void prepararNuevo() {
        idEdicion = null;
        codigo = null;
        porcentaje = null;
        fechaVencimiento = null;
        usosMaximos = 1;
    }

    public void prepararEdicion(Fila fila) {
        idEdicion = fila.getId();
        codigo = fila.getCodigo();
        porcentaje = fila.getPorcentaje();
        fechaVencimiento = fila.getFechaVencimiento();
        usosMaximos = fila.getUsosMaximos();
    }

    /** Crea o actualiza el cupón. El diálogo solo se cierra (parámetro "ok") si todo salió bien. */
    public void guardar() {
        try {
            if (porcentaje == null || porcentaje < 1 || porcentaje > 99) {
                aviso(FacesMessage.SEVERITY_WARN, "El porcentaje debe ser un número entero entre 1 y 99");
                return;
            }
            if (usosMaximos == null || usosMaximos < 1) {
                aviso(FacesMessage.SEVERITY_WARN, "Los usos máximos deben ser un número entero mayor o igual a 1");
                return;
            }
            Cupon c = new Cupon();
            c.setCodigo(codigo == null ? null : codigo.trim());
            c.setPorcentaje(porcentaje / 100.0);
            c.setFechaVencimiento(fechaVencimiento);
            c.setUsosMaximos(usosMaximos);
            if (idEdicion == null) {
                cuponServicio.registrarCupon(c);
                aviso(FacesMessage.SEVERITY_INFO, "Cupón creado: " + c.getCodigo().toUpperCase());
            } else {
                c.setId(idEdicion);
                cuponServicio.actualizarCupon(c);
                aviso(FacesMessage.SEVERITY_INFO, "Cupón actualizado: " + c.getCodigo().toUpperCase());
            }
            PrimeFaces.current().ajax().addCallbackParam("ok", true);
            recargar();
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("guardar el cupón", e);
        }
    }

    // ---------- Desactivar y eliminar ----------

    /** Desactiva el cupón poniendo su vencimiento en este momento. */
    public void desactivar(Fila fila) {
        try {
            Cupon c = cuponServicio.obtenerCupon(fila.getId());
            c.setFechaVencimiento(new Date());
            cuponServicio.actualizarCupon(c);
            aviso(FacesMessage.SEVERITY_INFO, "Cupón desactivado: " + fila.getCodigo());
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("desactivar el cupón", e);
        }
        recargar();
    }

    public void prepararEliminar(Fila fila) {
        idSeleccionado = fila.getId();
        codigoSeleccionado = fila.getCodigo();
    }

    /** Elimina el cupón; si ya se usó el servicio lo rechaza y se sugiere desactivarlo. */
    public void eliminar() {
        try {
            cuponServicio.eliminarCupon(idSeleccionado);
            aviso(FacesMessage.SEVERITY_INFO, "Cupón eliminado: " + codigoSeleccionado);
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage() + ". Puedes desactivarlo en su lugar.");
        } catch (Exception e) {
            errorInesperado("eliminar el cupón", e);
        }
        recargar();
    }

    private void errorInesperado(String accion, Exception e) {
        LOG.error("Error inesperado al {}", accion, e);
        aviso(FacesMessage.SEVERITY_ERROR, "No se pudo " + accion + ". Inténtalo de nuevo más tarde.");
    }

    private void aviso(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severidad, severidad == FacesMessage.SEVERITY_INFO ? "Listo" : "Atención", texto));
    }

    public List<Fila> getCupones() { return cupones; }
    public boolean isEdicion() { return idEdicion != null; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getPorcentaje() { return porcentaje; }
    public void setPorcentaje(Integer porcentaje) { this.porcentaje = porcentaje; }
    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public Integer getUsosMaximos() { return usosMaximos; }
    public void setUsosMaximos(Integer usosMaximos) { this.usosMaximos = usosMaximos; }
    public String getCodigoSeleccionado() { return codigoSeleccionado; }
}
