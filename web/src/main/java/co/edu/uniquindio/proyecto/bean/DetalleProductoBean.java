package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.HistorialPrecio;
import co.edu.uniquindio.proyecto.entidades.MotivoReporte;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.HistorialPrecioServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.ReporteProductoServicio;
import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean de {@code detalleProducto.xhtml?producto=ID}: datos del producto, galería, comentarios, calificación,
 * respuestas del vendedor, reporte y aviso de rebaja. Es ViewScoped y las entidades quedan separadas entre
 * peticiones, así que en {@link #cargar()} se copia lo que la vista necesita.
 */
@Component
@ViewScoped
public class DetalleProductoBean extends ListadoProductosBase {

    private final ComentarioServicio comentarioServicio;
    private final ReporteProductoServicio reporteServicio;
    private final HistorialPrecioServicio historialServicio;
    private final SeguridadBean seguridadBean;
    private final CarritoBean carritoBean;

    /** Valor crudo del parámetro {@code producto} (texto, para tolerar valores no numéricos). */
    private String idProducto;

    private Producto producto;
    private boolean existe;
    private boolean disponible;

    // Copias de lo que depende de relaciones perezosas
    private List<String> imagenes = new ArrayList<>();
    private String categorias = "";
    private String ciudad;
    private Long idVendedor;
    private String nombreVendedor;

    /** Precio final anterior si el producto tuvo una rebaja reciente; null si no. */
    private Double precioAntes;

    private List<Comentario> comentarios = new ArrayList<>();
    private double promedio;
    private boolean yaReporto;
    private boolean favorito;

    // Cantidad a agregar al carrito
    private int cantidad = 1;

    // Formulario de comentario
    private String mensaje;
    private int calificacion;

    // Respuestas del vendedor por id de comentario
    private Map<Long, String> respuestas = new HashMap<>();

    // Formulario de reporte
    private MotivoReporte motivo;
    private String descripcionReporte;

    public DetalleProductoBean(ProductoServicio productoServicio, ComentarioServicio comentarioServicio,
                               ReporteProductoServicio reporteServicio, HistorialPrecioServicio historialServicio,
                               SeguridadBean seguridadBean, CarritoBean carritoBean) {
        super(productoServicio, comentarioServicio);
        this.comentarioServicio = comentarioServicio;
        this.reporteServicio = reporteServicio;
        this.historialServicio = historialServicio;
        this.seguridadBean = seguridadBean;
        this.carritoBean = carritoBean;
    }

    /** Se ejecuta con f:viewAction: carga el producto o marca que no existe (sin lanzar error). */
    public void cargar() {
        existe = false;
        try {
            producto = productoServicio.obtenerProducto(Long.valueOf(idProducto == null ? "" : idProducto.trim()));
        } catch (NumberFormatException | NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, "El producto no existe");
            return;
        }
        existe = true;
        disponible = producto.getUnidades() > 0 && producto.getFechaLimite() != null
                && producto.getFechaLimite().after(new Date());
        imagenes = new ArrayList<>(producto.getMisImagenes());
        categorias = producto.getMisCategorias() == null ? "" : producto.getMisCategorias().stream()
                .map(Categoria::getNombre).collect(Collectors.joining(", "));
        ciudad = producto.getCiudad() == null ? null : producto.getCiudad().getNombre();
        Usuario vendedor = producto.getUsuario();
        idVendedor = vendedor.getId();
        nombreVendedor = vendedor.getNombre();
        calcularRebaja();
        recargarComentarios();
        recargarReporte();
        recargarFavorito();
    }

    /** Aviso "Antes $X": el último cambio de precio final fue a la baja y el precio actual sigue por debajo. */
    private void calcularRebaja() {
        precioAntes = null;
        try {
            List<HistorialPrecio> historial = historialServicio.listarPorProducto(producto.getId());
            if (historial.isEmpty()) {
                return;
            }
            HistorialPrecio h = historial.get(0);
            double antes = finalDe(h.getPrecioAnterior(), h.getDescuentoAnterior());
            double despues = finalDe(h.getPrecioNuevo(), h.getDescuentoNuevo());
            if (despues < antes && precioFinal(producto) < antes) {
                precioAntes = antes;
            }
        } catch (NegocioException e) {
            precioAntes = null;
        }
    }

    private double finalDe(Double precio, Double descuento) {
        return (precio == null ? 0.0 : precio) * (1 - (descuento == null ? 0.0 : descuento));
    }

    private void recargarComentarios() {
        comentarios = comentarioServicio.listarComentarioProducto(producto.getId());
        try {
            promedio = comentarioServicio.calcularCalificacion(producto.getId());
        } catch (NegocioException e) {
            promedio = 0;
        }
    }

    private void recargarReporte() {
        yaReporto = esUsuarioNoVendedor() && reporteServicio.yaReporto(seguridadBean.getIdUsuario(), producto.getId());
    }

    private void recargarFavorito() {
        favorito = esUsuarioNoVendedor() && productoServicio.esFavorito(producto.getId(), seguridadBean.getIdUsuario());
    }

    // ---------- Acciones ----------

    /** Agrega el producto a favoritos o lo quita, según su estado actual. */
    public void alternarFavorito() {
        if (!esUsuarioNoVendedor()) {
            aviso(FacesMessage.SEVERITY_WARN, "No puede guardar este producto en favoritos");
            return;
        }
        Long idUsuario = seguridadBean.getIdUsuario();
        try {
            if (productoServicio.esFavorito(producto.getId(), idUsuario)) {
                productoServicio.quitarFavorito(producto.getId(), idUsuario);
                aviso(FacesMessage.SEVERITY_INFO, "Quitado de favoritos");
            } else {
                productoServicio.agregarFavorito(producto.getId(), idUsuario);
                aviso(FacesMessage.SEVERITY_INFO, "Agregado a favoritos");
            }
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
        recargarFavorito();
    }

    /** Agrega al carrito la cantidad elegida (solo usuarios que no son el vendedor y con el producto disponible). */
    public void agregarAlCarrito() {
        if (!esUsuarioNoVendedor()) {
            aviso(FacesMessage.SEVERITY_WARN, "No puede agregar este producto al carrito");
            return;
        }
        try {
            // Se vuelve a leer el producto: las unidades y la vigencia pudieron cambiar
            Producto actual = productoServicio.obtenerProducto(producto.getId());
            if (actual.getFechaLimite() == null || !actual.getFechaLimite().after(new Date())) {
                aviso(FacesMessage.SEVERITY_WARN, "El producto ya no está disponible");
                return;
            }
            int total = carritoBean.agregar(actual, cantidad);
            aviso(FacesMessage.SEVERITY_INFO, "Agregado al carrito (en tu carrito: " + total + ")");
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    public void comentar() {
        if (!esUsuarioNoVendedor()) {
            aviso(FacesMessage.SEVERITY_WARN, "No puede comentar este producto");
            return;
        }
        Comentario comentario = new Comentario();
        comentario.setUsuario(referenciaUsuario(seguridadBean.getIdUsuario()));
        comentario.setProducto(producto);
        comentario.setMensaje(mensaje);
        comentario.setCalificacion(calificacion);
        try {
            comentarioServicio.comentarProducto(comentario);
            mensaje = null;
            calificacion = 0;
            recargarComentarios();
            aviso(FacesMessage.SEVERITY_INFO, "Comentario publicado");
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    public void responder(Long idComentario) {
        if (!esVendedor()) {
            aviso(FacesMessage.SEVERITY_WARN, "Solo el vendedor puede responder");
            return;
        }
        try {
            comentarioServicio.responderComentario(idComentario, respuestas.get(idComentario), idVendedor);
            respuestas.remove(idComentario);
            recargarComentarios();
            aviso(FacesMessage.SEVERITY_INFO, "Respuesta publicada");
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    public void reportar() {
        if (!esUsuarioNoVendedor()) {
            aviso(FacesMessage.SEVERITY_WARN, "No puede reportar este producto");
            return;
        }
        ReporteProducto reporte = new ReporteProducto();
        reporte.setUsuario(referenciaUsuario(seguridadBean.getIdUsuario()));
        reporte.setProducto(producto);
        reporte.setMotivo(motivo);
        reporte.setDescripcion(descripcionReporte == null || descripcionReporte.trim().isEmpty()
                ? null : descripcionReporte.trim());
        try {
            reporteServicio.reportarProducto(reporte);
            motivo = null;
            descripcionReporte = null;
            recargarReporte();
            PrimeFaces.current().executeScript("PF('dlgReporte').hide()");
            aviso(FacesMessage.SEVERITY_INFO, "Reporte enviado");
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
            recargarReporte();
        }
    }

    // ---------- Consultas para la vista ----------

    public boolean isVendedor() {
        return esVendedor();
    }

    public boolean isUsuarioNoVendedor() {
        return esUsuarioNoVendedor();
    }

    private boolean esVendedor() {
        return existe && seguridadBean.getIdUsuario() != null && seguridadBean.getIdUsuario().equals(idVendedor);
    }

    private boolean esUsuarioNoVendedor() {
        return existe && seguridadBean.isUsuario() && !esVendedor();
    }

    public boolean isTieneCalificacion() {
        return promedio > 0;
    }

    /** Promedio redondeado a estrellas enteras. */
    public int getEstrellasPromedio() {
        return (int) Math.round(promedio);
    }

    public MotivoReporte[] getMotivos() {
        return MotivoReporte.values();
    }

    public String etiqueta(MotivoReporte m) {
        switch (m) {
            case FRAUDE: return "Fraude";
            case PRODUCTO_PROHIBIDO: return "Producto prohibido";
            case INFORMACION_FALSA: return "Información falsa";
            case CONTENIDO_OFENSIVO: return "Contenido ofensivo";
            default: return "Otro";
        }
    }

    /** Ruta (desde la raíz de la aplicación) de una imagen del producto. */
    public String rutaImagen(String ruta) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/" + ruta;
    }

    /** Primera imagen para la galería (o la imagen por defecto si no hay). */
    public String getImagenPrincipal() {
        return imagenes.isEmpty() ? imagen(producto) : rutaImagen(imagenes.get(0));
    }

    private static Usuario referenciaUsuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }

    private static void aviso(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad,
                severidad == FacesMessage.SEVERITY_INFO ? "Listo" : "Atención", texto));
    }

    // ---------- Getters y setters ----------

    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }
    public Producto getProducto() { return producto; }
    public boolean isExiste() { return existe; }
    public boolean isDisponible() { return disponible; }
    public List<String> getImagenes() { return imagenes; }
    public String getCategorias() { return categorias; }
    public String getCiudad() { return ciudad; }
    public String getNombreVendedor() { return nombreVendedor; }
    public Double getPrecioAntes() { return precioAntes; }
    public List<Comentario> getComentarios() { return comentarios; }
    public double getPromedio() { return promedio; }
    public boolean isFavorito() { return favorito; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public boolean isYaReporto() { return yaReporto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }
    public Map<Long, String> getRespuestas() { return respuestas; }
    public MotivoReporte getMotivo() { return motivo; }
    public void setMotivo(MotivoReporte motivo) { this.motivo = motivo; }
    public String getDescripcionReporte() { return descripcionReporte; }
    public void setDescripcionReporte(String descripcionReporte) { this.descripcionReporte = descripcionReporte; }
}
