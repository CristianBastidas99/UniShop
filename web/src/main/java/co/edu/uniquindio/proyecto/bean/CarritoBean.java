package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.dto.ItemCompraDTO;
import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CompraServicio;
import co.edu.uniquindio.proyecto.servicios.CuponServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Carrito de compras del usuario: vive en la sesión (desaparece al cerrarla). Guarda datos simples de cada
 * producto (no entidades) y calcula los precios igual que {@code CompraServicio}: precio con descuento del
 * producto, cupón aplicado a cada unidad y redondeo a 2 decimales (HALF_UP), de modo que el total mostrado
 * coincida con el que se guarda en la compra.
 */
@Component
@SessionScope
public class CarritoBean implements Serializable {

    /** Línea del carrito: un producto con su cantidad. */
    public static class Linea implements Serializable {
        private final Long idProducto;
        private final String nombre;
        private final String imagen;
        private BigDecimal precio; // con el descuento propio del producto (se actualiza al refrescar)
        private int cantidad;
        private int disponibles;

        Linea(Long idProducto, String nombre, String imagen, BigDecimal precio, int cantidad, int disponibles) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.imagen = imagen;
            this.precio = precio;
            this.cantidad = cantidad;
            this.disponibles = disponibles;
        }

        public Long getIdProducto() { return idProducto; }
        public String getNombre() { return nombre; }
        /** Ruta de la imagen desde la raíz de la aplicación. */
        public String getImagen() { return imagen; }
        public BigDecimal getPrecio() { return precio; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public int getDisponibles() { return disponibles; }
        /** Máximo del selector de cantidad (al menos 1). */
        public int getMaximo() { return Math.max(1, disponibles); }
        public boolean isExcedido() { return cantidad > disponibles; }
    }

    private static final String IMAGEN_POR_DEFECTO = "resources/img/sin-imagen.svg";

    private final ProductoServicio productoServicio;
    private final CompraServicio compraServicio;
    private final CuponServicio cuponServicio;
    private final SeguridadBean seguridadBean;

    private final List<Linea> lineas = new ArrayList<>();

    // Cupón aplicado (código y fracción 0 < p <= 1)
    private String cuponAplicado;
    private BigDecimal porcentajeCupon;

    // Campos del formulario del carrito
    private String codigoCupon;
    private MedioPago medioPago;

    public CarritoBean(ProductoServicio productoServicio, CompraServicio compraServicio,
                       CuponServicio cuponServicio, SeguridadBean seguridadBean) {
        this.productoServicio = productoServicio;
        this.compraServicio = compraServicio;
        this.cuponServicio = cuponServicio;
        this.seguridadBean = seguridadBean;
    }

    // ---------- Operaciones sobre las líneas ----------

    /**
     * Agrega el producto; si ya está, suma la cantidad sin pasar de las unidades disponibles.
     *
     * @return cantidad total del producto en el carrito tras agregar
     * @throws NegocioException si no hay unidades que agregar
     */
    public int agregar(Producto producto, int cantidad) throws NegocioException {
        int disponibles = producto.getUnidades();
        if (cantidad < 1 || disponibles < 1) {
            throw new NegocioException("No hay unidades disponibles de " + producto.getNombre());
        }
        Linea existente = buscar(producto.getId());
        if (existente == null) {
            String ruta = producto.getMisImagenes() == null || producto.getMisImagenes().isEmpty()
                    ? IMAGEN_POR_DEFECTO : producto.getMisImagenes().get(0);
            int inicial = Math.min(cantidad, disponibles);
            lineas.add(new Linea(producto.getId(), producto.getNombre(), ruta,
                    BigDecimal.valueOf(productoServicio.calcularPrecioConDescuento(producto)),
                    inicial, disponibles));
            return inicial;
        }
        existente.disponibles = disponibles;
        int nueva = Math.min(existente.cantidad + cantidad, disponibles);
        if (nueva <= existente.cantidad) {
            throw new NegocioException("Ya tienes en el carrito todas las unidades disponibles de "
                    + producto.getNombre());
        }
        existente.cantidad = nueva;
        return nueva;
    }

    /** Ajusta la cantidad de una línea al rango 1..disponibles (se usa tras editarla en la tabla). */
    public void ajustarCantidad(Linea linea) {
        if (linea.cantidad < 1) {
            linea.cantidad = 1;
        }
        if (linea.disponibles >= 1 && linea.cantidad > linea.disponibles) {
            linea.cantidad = linea.disponibles;
            aviso(FacesMessage.SEVERITY_WARN, "Solo hay " + linea.disponibles + " unidades de " + linea.nombre);
        }
    }

    public void quitar(Long idProducto) {
        lineas.removeIf(l -> l.idProducto.equals(idProducto));
        aviso(FacesMessage.SEVERITY_INFO, "Producto quitado del carrito");
    }

    /** Vacía el carrito y deja sin cupón ni medio de pago. */
    public void vaciar() {
        lineas.clear();
        cuponAplicado = null;
        porcentajeCupon = null;
        codigoCupon = null;
        medioPago = null;
    }

    private Linea buscar(Long idProducto) {
        return lineas.stream().filter(l -> l.idProducto.equals(idProducto)).findFirst().orElse(null);
    }

    /** Actualiza el precio y las unidades disponibles de cada línea (y quita los productos que ya no existen). */
    public void refrescar() {
        for (Iterator<Linea> it = lineas.iterator(); it.hasNext(); ) {
            Linea linea = it.next();
            try {
                Producto p = productoServicio.obtenerProducto(linea.idProducto);
                boolean vigente = p.getFechaLimite() != null && p.getFechaLimite().after(new Date());
                linea.disponibles = vigente ? p.getUnidades() : 0;
                // El vendedor pudo cambiar el precio o el descuento: el total debe coincidir con el de la compra
                linea.precio = BigDecimal.valueOf(productoServicio.calcularPrecioConDescuento(p));
            } catch (NegocioException e) {
                it.remove();
            }
        }
    }

    // ---------- Cupón ----------

    public void aplicarCupon() {
        if (codigoCupon == null || codigoCupon.trim().isEmpty()) {
            aviso(FacesMessage.SEVERITY_WARN, "Escribe el código del cupón");
            return;
        }
        try {
            Cupon cupon = cuponServicio.obtenerDisponible(codigoCupon.trim());
            cuponAplicado = cupon.getCodigo();
            porcentajeCupon = BigDecimal.valueOf(cupon.getPorcentaje());
            codigoCupon = null;
            aviso(FacesMessage.SEVERITY_INFO, "Cupón " + cuponAplicado + " aplicado");
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    public void quitarCupon() {
        cuponAplicado = null;
        porcentajeCupon = null;
        aviso(FacesMessage.SEVERITY_INFO, "Cupón quitado");
    }

    // ---------- Cálculos ----------

    /** Precio unitario final: descuento del producto y, si hay cupón, el del cupón; 2 decimales. */
    public BigDecimal precioUnitario(Linea linea) {
        BigDecimal precio = linea.precio;
        if (porcentajeCupon != null) {
            precio = precio.multiply(BigDecimal.ONE.subtract(porcentajeCupon));
        }
        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    /** Subtotal de la línea con el cupón aplicado. */
    public BigDecimal subtotalLinea(Linea linea) {
        return precioUnitario(linea).multiply(BigDecimal.valueOf(linea.cantidad));
    }

    /** Suma sin cupón (con el descuento propio de cada producto). */
    public BigDecimal getSubtotal() {
        return lineas.stream().map(l -> l.precio.multiply(BigDecimal.valueOf(l.cantidad)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Total a pagar: igual al que guarda la compra. */
    public BigDecimal getTotal() {
        return lineas.stream().map(this::subtotalLinea).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Descuento que aporta el cupón (subtotal menos total). */
    public BigDecimal getDescuentoCupon() {
        return getSubtotal().subtract(getTotal());
    }

    // ---------- Compra ----------

    /** Confirma la compra. Devuelve el outcome de redirección si todo va bien, o null si falla. */
    public String confirmar() {
        if (lineas.isEmpty()) {
            aviso(FacesMessage.SEVERITY_WARN, "Tu carrito está vacío");
            return null;
        }
        if (medioPago == null) {
            aviso(FacesMessage.SEVERITY_WARN, "Selecciona un medio de pago");
            return null;
        }
        List<ItemCompraDTO> items = lineas.stream()
                .map(l -> new ItemCompraDTO(l.idProducto, l.cantidad)).collect(Collectors.toList());
        try {
            compraServicio.comprar(seguridadBean.getIdUsuario(), items, medioPago, cuponAplicado);
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
            refrescar();
            return null;
        }
        vaciar();
        aviso(FacesMessage.SEVERITY_INFO, "Compra realizada");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/usuario/compras?faces-redirect=true";
    }

    // ---------- Consultas para la vista ----------

    public List<Linea> getLineas() { return lineas; }

    /** Cantidad de productos distintos en el carrito. */
    public int getCantidadProductos() { return lineas.size(); }

    public boolean isVacio() { return lineas.isEmpty(); }

    public boolean isConCupon() { return cuponAplicado != null; }

    public String getCuponAplicado() { return cuponAplicado; }

    /** Porcentaje del cupón como entero (por ejemplo, 20). */
    public int getPorcentajeCupon() {
        return porcentajeCupon == null ? 0 : porcentajeCupon.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public MedioPago[] getMediosPago() { return MedioPago.values(); }

    public String etiqueta(MedioPago m) {
        return m.getEtiqueta();
    }

    /** Ruta (desde la raíz de la aplicación) de la imagen de una línea. */
    public String rutaImagen(Linea linea) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/" + linea.imagen;
    }

    public String getCodigoCupon() { return codigoCupon; }
    public void setCodigoCupon(String codigoCupon) { this.codigoCupon = codigoCupon; }
    public MedioPago getMedioPago() { return medioPago; }
    public void setMedioPago(MedioPago medioPago) { this.medioPago = medioPago; }

    private static void aviso(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad,
                severidad == FacesMessage.SEVERITY_INFO ? "Listo" : "Atención", texto));
    }
}
