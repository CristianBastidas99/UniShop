package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.servicios.CuponServicio;
import co.edu.uniquindio.proyecto.servicios.ReporteProductoServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.Date;

/** Bean de {@code admin/inicio.xhtml}: calcula los números de las tarjetas de acceso del panel. */
@Component
@ViewScoped
public class PanelAdminBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(PanelAdminBean.class);

    private final UsuarioServicio usuarioServicio;
    private final ReporteProductoServicio reporteServicio;
    private final CuponServicio cuponServicio;
    private final SeguridadBean seguridadBean;

    private int totalUsuarios;
    private int usuariosDesactivados;
    private int reportesPorRevisar;
    private int cuponesVigentes;
    private int totalCupones;

    public PanelAdminBean(UsuarioServicio usuarioServicio, ReporteProductoServicio reporteServicio,
                          CuponServicio cuponServicio, SeguridadBean seguridadBean) {
        this.usuarioServicio = usuarioServicio;
        this.reporteServicio = reporteServicio;
        this.cuponServicio = cuponServicio;
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        try {
            totalUsuarios = 0;
            usuariosDesactivados = 0;
            for (Usuario u : usuarioServicio.listarUsuarios()) {
                totalUsuarios++;
                if (!u.isActivo()) {
                    usuariosDesactivados++;
                }
            }
            reportesPorRevisar = reporteServicio.listarPorRevisar().size();
            Date ahora = new Date();
            totalCupones = 0;
            cuponesVigentes = 0;
            for (Cupon c : cuponServicio.listarCupones()) {
                totalCupones++;
                if (c.getFechaVencimiento() != null && c.getFechaVencimiento().after(ahora)
                        && c.getUsosRealizados() < c.getUsosMaximos()) {
                    cuponesVigentes++;
                }
            }
        } catch (Exception e) {
            LOG.error("Error inesperado al cargar el panel del administrador", e);
        }
    }

    public String getNombre() { return seguridadBean.getNombre(); }
    public int getTotalUsuarios() { return totalUsuarios; }
    public int getUsuariosDesactivados() { return usuariosDesactivados; }
    public int getReportesPorRevisar() { return reportesPorRevisar; }
    public int getCuponesVigentes() { return cuponesVigentes; }
    public int getTotalCupones() { return totalCupones; }
}
