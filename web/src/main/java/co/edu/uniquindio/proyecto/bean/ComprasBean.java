package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CompraServicio;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Bean de {@code usuario/compras.xhtml}: lista las compras del usuario en sesión, la más reciente primero.
 * Copia los datos a objetos simples para no depender de la sesión de Hibernate al pintar la vista.
 */
@Component
@ViewScoped
public class ComprasBean implements Serializable {

    /** Línea de una compra ya copiada a datos simples. */
    public static class Linea implements Serializable {
        private final Long idProducto;
        private final String producto;
        private final int cantidad;
        private final BigDecimal precioUnitario;

        Linea(Long idProducto, String producto, int cantidad, BigDecimal precioUnitario) {
            this.idProducto = idProducto;
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public Long getIdProducto() { return idProducto; }
        public String getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public BigDecimal getSubtotal() { return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); }
    }

    /** Compra copiada a datos simples. */
    public static class Resumen implements Serializable {
        private final Long id;
        private final String fecha;
        private final String medioPago;
        private final String cupon;
        private final int porcentajeCupon;
        private final List<Linea> lineas;
        private final BigDecimal total;

        Resumen(Long id, String fecha, String medioPago, String cupon, int porcentajeCupon,
                List<Linea> lineas, BigDecimal total) {
            this.id = id;
            this.fecha = fecha;
            this.medioPago = medioPago;
            this.cupon = cupon;
            this.porcentajeCupon = porcentajeCupon;
            this.lineas = lineas;
            this.total = total;
        }

        public Long getId() { return id; }
        public String getFecha() { return fecha; }
        public String getMedioPago() { return medioPago; }
        public String getCupon() { return cupon; }
        public int getPorcentajeCupon() { return porcentajeCupon; }
        public boolean isConCupon() { return cupon != null; }
        public List<Linea> getLineas() { return lineas; }
        public BigDecimal getTotal() { return total; }
    }

    private final CompraServicio compraServicio;
    private final SeguridadBean seguridadBean;

    private List<Resumen> compras = new ArrayList<>();

    public ComprasBean(CompraServicio compraServicio, SeguridadBean seguridadBean) {
        this.compraServicio = compraServicio;
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        compras = new ArrayList<>();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formato.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
        try {
            for (Compra c : compraServicio.listarComprasUsuario(seguridadBean.getIdUsuario())) {
                List<Linea> lineas = new ArrayList<>();
                for (DetalleCompra d : compraServicio.listarDetalles(c.getId())) {
                    lineas.add(new Linea(d.getProducto().getId(), d.getProducto().getNombre(), d.getUnidades(),
                            BigDecimal.valueOf(d.getPrecioProducto()).setScale(2, RoundingMode.HALF_UP)));
                }
                BigDecimal total = BigDecimal.valueOf(compraServicio.calcularTotal(c.getId()));
                String cupon = c.getCupon() == null ? null : c.getCupon().getCodigo();
                int porcentaje = c.getCupon() == null ? 0 : BigDecimal.valueOf(c.getCupon().getPorcentaje())
                        .multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
                compras.add(new Resumen(c.getId(), formato.format(c.getFechaCompra()),
                        c.getMedioPago().getEtiqueta(), cupon, porcentaje, lineas, total));
            }
        } catch (NegocioException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", e.getMessage()));
        }
    }

    public List<Resumen> getCompras() { return compras; }
}
