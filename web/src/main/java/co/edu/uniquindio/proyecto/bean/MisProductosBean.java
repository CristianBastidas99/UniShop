package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean de {@code usuario/misProductos.xhtml}: lista los productos del usuario en sesión y permite poner o
 * quitar el descuento y eliminar. Copia los datos a filas simples para no depender de la sesión de Hibernate.
 * La edición se hace en {@code registraProducto.xhtml?producto=ID}.
 */
@Component
@ViewScoped
public class MisProductosBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MisProductosBean.class);

    private static final String IMAGEN_POR_DEFECTO = "resources/img/sin-imagen.svg";

    /** Producto del usuario ya copiado a datos simples. */
    public static class Fila implements Serializable {
        private final Long id;
        private final String nombre;
        private final String imagen;
        private final double precio;
        private final int porcentaje;
        private final double precioFinal;
        private final int unidades;
        private final Date fechaLimite;
        private final String estado;
        private Long idSubastaActiva;

        Fila(Long id, String nombre, String imagen, double precio, int porcentaje, double precioFinal,
             int unidades, Date fechaLimite, String estado) {
            this.id = id;
            this.nombre = nombre;
            this.imagen = imagen;
            this.precio = precio;
            this.porcentaje = porcentaje;
            this.precioFinal = precioFinal;
            this.unidades = unidades;
            this.fechaLimite = fechaLimite;
            this.estado = estado;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getImagen() { return imagen; }
        public double getPrecio() { return precio; }
        public int getPorcentaje() { return porcentaje; }
        public boolean isConDescuento() { return porcentaje > 0; }
        public double getPrecioFinal() { return precioFinal; }
        public int getUnidades() { return unidades; }
        public Date getFechaLimite() { return fechaLimite; }
        public String getEstado() { return estado; }
        public boolean isVigente() { return "Vigente".equals(estado); }
        public Long getIdSubastaActiva() { return idSubastaActiva; }
        public boolean isEnSubasta() { return idSubastaActiva != null; }
    }

    private final ProductoServicio productoServicio;
    private final SubastaServicio subastaServicio;
    private final SeguridadBean seguridadBean;

    @Value("${upload.url}")
    private String urlImagenes;

    private List<Fila> productos = new ArrayList<>();

    // Producto elegido en los diálogos de descuento y de eliminación
    private Long idSeleccionado;
    private String nombreSeleccionado;
    private Integer porcentaje;

    public MisProductosBean(ProductoServicio productoServicio, SubastaServicio subastaServicio,
                            SeguridadBean seguridadBean) {
        this.productoServicio = productoServicio;
        this.subastaServicio = subastaServicio;
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        recargar();
    }

    private void recargar() {
        productos = new ArrayList<>();
        try {
            String contexto = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
            Date ahora = new Date();
            Map<Long, Long> subastasActivas = new HashMap<>();
            for (Subasta s : subastaServicio.listarActivas()) {
                subastasActivas.put(s.getProducto().getId(), s.getId());
            }
            for (Producto p : productoServicio.listarPorVendedor(seguridadBean.getIdUsuario())) {
                String imagen = p.getMisImagenes() == null || p.getMisImagenes().isEmpty()
                        ? IMAGEN_POR_DEFECTO : p.getMisImagenes().get(0);
                int porcentajeProducto = p.getDescuento() == null ? 0 : (int) Math.round(p.getDescuento() * 100);
                String estado;
                if (p.getFechaLimite() == null || !p.getFechaLimite().after(ahora)) {
                    estado = "Vencido";
                } else if (p.getUnidades() <= 0) {
                    estado = "Sin unidades";
                } else {
                    estado = "Vigente";
                }
                Fila fila = new Fila(p.getId(), p.getNombre(), contexto + "/" + imagen, p.getPrecio(),
                        porcentajeProducto, productoServicio.calcularPrecioConDescuento(p), p.getUnidades(),
                        p.getFechaLimite(), estado);
                fila.idSubastaActiva = subastasActivas.get(p.getId());
                productos.add(fila);
            }
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    // ---------- Descuento ----------

    /** Prepara el diálogo de descuento con el porcentaje actual (si lo hay). */
    public void prepararDescuento(Fila fila) {
        idSeleccionado = fila.getId();
        nombreSeleccionado = fila.getNombre();
        porcentaje = fila.isConDescuento() ? fila.getPorcentaje() : null;
    }

    /** Aplica el porcentaje entero (1 a 99) como fracción 0 a 1. */
    public void guardarDescuento() {
        if (porcentaje == null || porcentaje < 1 || porcentaje > 99) {
            aviso(FacesMessage.SEVERITY_WARN, "El descuento debe ser un número entero entre 1 y 99");
            return;
        }
        try {
            productoServicio.ponerDescuento(idSeleccionado, seguridadBean.getIdUsuario(), porcentaje / 100.0);
            aviso(FacesMessage.SEVERITY_INFO, "Descuento del " + porcentaje + " % aplicado a " + nombreSeleccionado);
            PrimeFaces.current().executeScript("PF('dlgDescuento').hide()");
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("poner el descuento", e);
        }
        recargar();
    }

    public void quitarDescuento(Fila fila) {
        try {
            productoServicio.quitarDescuento(fila.getId(), seguridadBean.getIdUsuario());
            aviso(FacesMessage.SEVERITY_INFO, "Descuento quitado de " + fila.getNombre());
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("quitar el descuento", e);
        }
        recargar();
    }

    // ---------- Eliminar ----------

    public void prepararEliminar(Fila fila) {
        idSeleccionado = fila.getId();
        nombreSeleccionado = fila.getNombre();
    }

    /**
     * Elimina el producto. El servicio lo borra si no tiene compras, comentarios, subastas, chats ni reportes;
     * si los tiene lo invalida (fecha límite en el momento actual) y así lo indica el mensaje.
     */
    public void eliminar() {
        try {
            // Rutas de las imágenes, para borrar los archivos si el producto se elimina de verdad
            List<String> rutas = new ArrayList<>();
            try {
                Producto actual = productoServicio.obtenerProducto(idSeleccionado);
                if (actual.getMisImagenes() != null) {
                    rutas.addAll(actual.getMisImagenes());
                }
            } catch (NegocioException e) {
                // El servicio dará el mensaje al intentar eliminar
            }
            productoServicio.eliminarProducto(idSeleccionado, seguridadBean.getIdUsuario());
            boolean sigueExistiendo;
            try {
                productoServicio.obtenerProducto(idSeleccionado);
                sigueExistiendo = true;
            } catch (NegocioException e) {
                sigueExistiendo = false;
            }
            if (sigueExistiendo) {
                aviso(FacesMessage.SEVERITY_INFO, nombreSeleccionado + " tiene compras, comentarios u otra actividad, "
                        + "por lo que no se borró: quedó invalidado (vencido) y ya no se muestra al público");
            } else {
                rutas.forEach(this::borrarArchivo);
                aviso(FacesMessage.SEVERITY_INFO, "Producto eliminado: " + nombreSeleccionado);
            }
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("eliminar el producto", e);
        }
        recargar();
    }

    /** Borra un archivo de la carpeta de subidas; ignora las imágenes que no estén allí (p. ej. la de ejemplo). */
    private void borrarArchivo(String ruta) {
        if (ruta == null || !ruta.startsWith("uploads/")) {
            return;
        }
        String nombre = ruta.substring("uploads/".length());
        if (nombre.isEmpty() || nombre.contains("/") || nombre.contains("\\") || nombre.contains("..")) {
            return;
        }
        File archivo = new File(urlImagenes, nombre);
        if (archivo.isFile() && !archivo.delete()) {
            LOG.warn("No se pudo borrar la imagen {}", archivo);
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

    public List<Fila> getProductos() { return productos; }
    public String getNombreSeleccionado() { return nombreSeleccionado; }
    public Integer getPorcentaje() { return porcentaje; }
    public void setPorcentaje(Integer porcentaje) { this.porcentaje = porcentaje; }
}
