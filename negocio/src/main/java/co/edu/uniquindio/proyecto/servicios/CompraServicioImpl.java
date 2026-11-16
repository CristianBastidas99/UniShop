package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.ItemCompraDTO;
import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import co.edu.uniquindio.proyecto.repositorios.DetalleCompraRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompraServicioImpl implements CompraServicio {

    private final CompraRepo compraRepo;
    private final DetalleCompraRepo detalleCompraRepo;
    private final UsuarioRepo usuarioRepo;
    private final ProductoRepo productoRepo;
    private final ProductoServicio productoServicio;
    private final CuponServicio cuponServicio;

    public CompraServicioImpl(CompraRepo compraRepo, DetalleCompraRepo detalleCompraRepo, UsuarioRepo usuarioRepo,
                              ProductoRepo productoRepo, ProductoServicio productoServicio,
                              CuponServicio cuponServicio) {
        this.compraRepo = compraRepo;
        this.detalleCompraRepo = detalleCompraRepo;
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
        this.productoServicio = productoServicio;
        this.cuponServicio = cuponServicio;
    }

    @Override
    public Compra registrarCompra(Compra compra) throws NegocioException {
        if (compra.getId() != null && compraRepo.existsById(compra.getId())) {
            throw new NegocioException("La compra ya existe");
        }
        return compraRepo.save(compra);
    }

    @Override
    public Compra actualizarCompra(Compra compra) throws NegocioException {
        if (compra.getId() == null || !compraRepo.existsById(compra.getId())) {
            throw new NegocioException("La compra no existe");
        }
        return compraRepo.save(compra);
    }

    @Override
    public Compra obtenerCompra(Long id) throws NegocioException {
        return compraRepo.findById(id)
                .orElseThrow(() -> new NegocioException("La compra no existe"));
    }

    @Override
    public void eliminarCompra(Long id) throws NegocioException {
        Compra buscado = obtenerCompra(id);
        compraRepo.delete(buscado);
    }

    @Override
    public List<Compra> listarCompra() {
        return compraRepo.findAll();
    }

    @Override
    @Transactional(rollbackFor = NegocioException.class)
    public Compra comprar(Long idUsuario, List<ItemCompraDTO> items, MedioPago medioPago, String codigoCupon)
            throws NegocioException {
        // ===== Validaciones: nada se escribe hasta que todo sea válido =====
        Usuario usuario = cargarUsuario(idUsuario);
        if (medioPago == null) {
            throw new NegocioException("El medio de pago es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            throw new NegocioException("La compra debe tener al menos un producto");
        }
        Cupon cupon = null;
        if (codigoCupon != null && !codigoCupon.trim().isEmpty()) {
            cupon = cuponServicio.obtenerDisponible(codigoCupon.trim());
        }

        // Agrupa por producto: si se repite, se suman las unidades
        Map<Long, Integer> pedidas = new LinkedHashMap<>();
        for (ItemCompraDTO item : items) {
            if (item == null || item.getIdProducto() == null) {
                throw new NegocioException("El producto no existe");
            }
            if (item.getUnidades() <= 0) {
                throw new NegocioException("Las unidades deben ser mayores que cero");
            }
            pedidas.merge(item.getIdProducto(), item.getUnidades(), Integer::sum);
        }

        Date ahora = new Date();
        Map<Producto, Integer> productos = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> pedido : pedidas.entrySet()) {
            Producto producto = productoRepo.findById(pedido.getKey())
                    .orElseThrow(() -> new NegocioException("El producto no existe"));
            if (producto.getFechaLimite() == null || !producto.getFechaLimite().after(ahora)) {
                throw new NegocioException("El producto " + producto.getNombre() + " no está disponible");
            }
            if (idUsuario.equals(producto.getUsuario().getId())) {
                throw new NegocioException("No puede comprar su propio producto");
            }
            if (pedido.getValue() > producto.getUnidades()) {
                throw new NegocioException("No hay suficientes unidades de " + producto.getNombre()
                        + " (disponibles: " + producto.getUnidades() + ")");
            }
            productos.put(producto, pedido.getValue());
        }

        // ===== Escritura =====
        Compra compra = new Compra();
        compra.setFechaCompra(ahora);
        compra.setMedioPago(medioPago);
        compra.setUsuario(usuario);
        compra.setCupon(cupon);
        compra = compraRepo.save(compra);

        List<DetalleCompra> detalles = new ArrayList<>();
        for (Map.Entry<Producto, Integer> entrada : productos.entrySet()) {
            Producto producto = entrada.getKey();
            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setUnidades(entrada.getValue());
            detalle.setPrecioProducto(calcularPrecioUnitario(producto, cupon));
            detalles.add(detalleCompraRepo.save(detalle));

            producto.setUnidades(producto.getUnidades() - entrada.getValue());
            productoRepo.save(producto);
        }
        compra.setMisDetalleCompras(detalles);

        if (cupon != null) {
            cupon.setUsosRealizados(cupon.getUsosRealizados() + 1);
        }
        return compra;
    }

    @Override
    public List<Compra> listarComprasUsuario(Long idUsuario) throws NegocioException {
        cargarUsuario(idUsuario);
        return compraRepo.listarPorUsuario(idUsuario);
    }

    @Override
    public List<DetalleCompra> listarDetalles(Long idCompra) throws NegocioException {
        obtenerCompraExistente(idCompra);
        return detalleCompraRepo.listarPorCompra(idCompra);
    }

    @Override
    public double calcularTotal(Long idCompra) throws NegocioException {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompra detalle : listarDetalles(idCompra)) {
            total = total.add(BigDecimal.valueOf(detalle.getPrecioProducto())
                    .multiply(BigDecimal.valueOf(detalle.getUnidades())));
        }
        return total.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // ===== Utilidades privadas =====

    private Usuario cargarUsuario(Long idUsuario) throws NegocioException {
        if (idUsuario == null) {
            throw new NegocioException("El usuario no existe");
        }
        return usuarioRepo.findById(idUsuario).orElseThrow(() -> new NegocioException("El usuario no existe"));
    }

    private void obtenerCompraExistente(Long idCompra) throws NegocioException {
        if (idCompra == null || !compraRepo.existsById(idCompra)) {
            throw new NegocioException("La compra no existe");
        }
    }

    /** Precio con descuento del producto y, si hay cupón, con el porcentaje del cupón aplicado. */
    private double calcularPrecioUnitario(Producto producto, Cupon cupon) {
        BigDecimal precio = BigDecimal.valueOf(productoServicio.calcularPrecioConDescuento(producto));
        if (cupon != null) {
            precio = precio.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(cupon.getPorcentaje())));
        }
        return precio.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

}
