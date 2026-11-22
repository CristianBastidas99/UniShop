package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.HistorialPrecio;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.excepciones.NoEncontradoException;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.CiudadRepo;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import co.edu.uniquindio.proyecto.repositorios.DetalleCompraRepo;
import co.edu.uniquindio.proyecto.repositorios.HistorialPrecioRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.ReporteProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ProductoServicioImpl implements ProductoServicio {

    private final ProductoRepo productoRepo;
    private final UsuarioRepo usuarioRepo;
    private final CiudadRepo ciudadRepo;
    private final CategoriaRepo categoriaRepo;
    private final HistorialPrecioRepo historialPrecioRepo;
    private final DetalleCompraRepo detalleCompraRepo;
    private final SubastaRepo subastaRepo;
    private final ComentarioRepo comentarioRepo;
    private final ChatRepo chatRepo;
    private final ReporteProductoRepo reporteProductoRepo;

    public ProductoServicioImpl(ProductoRepo productoRepo, UsuarioRepo usuarioRepo, CiudadRepo ciudadRepo,
                                CategoriaRepo categoriaRepo, HistorialPrecioRepo historialPrecioRepo,
                                DetalleCompraRepo detalleCompraRepo, SubastaRepo subastaRepo,
                                ComentarioRepo comentarioRepo, ChatRepo chatRepo,
                                ReporteProductoRepo reporteProductoRepo) {
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
        this.ciudadRepo = ciudadRepo;
        this.categoriaRepo = categoriaRepo;
        this.historialPrecioRepo = historialPrecioRepo;
        this.detalleCompraRepo = detalleCompraRepo;
        this.subastaRepo = subastaRepo;
        this.comentarioRepo = comentarioRepo;
        this.chatRepo = chatRepo;
        this.reporteProductoRepo = reporteProductoRepo;
    }

    @Override
    @Transactional
    public Producto publicarProducto(Producto producto, List<Long> idsCategorias) throws NegocioException {
        if (producto == null) {
            throw new NegocioException("El producto es obligatorio");
        }
        if (producto.getId() != null) {
            throw new NegocioException("El producto ya existe");
        }
        if (producto.getUsuario() == null || producto.getUsuario().getId() == null) {
            throw new NegocioException("El vendedor es obligatorio");
        }
        Usuario vendedor = usuarioRepo.findById(producto.getUsuario().getId())
                .orElseThrow(() -> new NegocioException("El vendedor no existe"));
        Ciudad ciudad = validarCiudad(producto.getCiudad());
        validarImagenes(producto);
        List<Categoria> categorias = cargarCategorias(idsCategorias);
        if (categorias.isEmpty()) {
            throw new NegocioException("Debe elegir al menos una categoría");
        }
        if (producto.getFechaLimite() == null || !producto.getFechaLimite().after(new Date())) {
            throw new NegocioException("La fecha límite debe ser posterior a la fecha actual");
        }
        if (producto.getUnidades() <= 0) {
            throw new NegocioException("Las unidades deben ser mayores que cero");
        }
        validarPrecio(producto.getPrecio());
        producto.setDescuento(validarDescuentoNuevo(producto.getDescuento()));

        producto.setUsuario(vendedor);
        producto.setCiudad(ciudad);
        Producto guardado = productoRepo.save(producto);
        for (Categoria categoria : categorias) {
            agregarACategoria(categoria, guardado);
        }
        return guardado;
    }

    @Override
    @Transactional
    public Producto actualizarProducto(Producto producto, List<Long> idsCategorias, Long idUsuario)
            throws NegocioException {
        if (producto == null) {
            throw new NegocioException("El producto no existe");
        }
        Producto actual = obtenerProducto(producto.getId());
        validarDueno(actual, idUsuario, "Solo el vendedor puede modificar el producto");
        Ciudad ciudad = validarCiudad(producto.getCiudad());
        validarImagenes(producto);
        List<Categoria> nuevas = null;
        if (idsCategorias != null) {
            nuevas = cargarCategorias(idsCategorias);
            if (nuevas.isEmpty()) {
                throw new NegocioException("Debe elegir al menos una categoría");
            }
        }
        if (producto.getUnidades() < 0) {
            throw new NegocioException("Las unidades no pueden ser negativas");
        }
        validarPrecio(producto.getPrecio());
        Double descuentoNuevo = validarDescuentoNuevo(producto.getDescuento());

        // Valores anteriores, leídos antes de modificar la entidad administrada
        Double precioAnterior = actual.getPrecio();
        Double descuentoAnterior = descuentoDe(actual);

        actual.setNombre(producto.getNombre());
        actual.setDescripcion(producto.getDescripcion());
        actual.setUnidades(producto.getUnidades());
        actual.setPrecio(producto.getPrecio());
        actual.setDescuento(descuentoNuevo);
        actual.setCiudad(ciudad);
        if (producto.getFechaLimite() != null) {
            actual.setFechaLimite(producto.getFechaLimite());
        }
        actual.setMisImagenes(new ArrayList<>(producto.getMisImagenes()));
        Producto guardado = productoRepo.save(actual);

        if (nuevas != null) {
            sincronizarCategorias(guardado, nuevas);
        }
        registrarCambioDePrecio(guardado, precioAnterior, descuentoAnterior);
        return guardado;
    }

    @Override
    @Transactional
    public void eliminarProducto(Long idProducto, Long idUsuario) throws NegocioException {
        Producto producto = obtenerProducto(idProducto);
        validarDueno(producto, idUsuario, "Solo el vendedor puede eliminar el producto");
        if (tieneDependientes(idProducto)) {
            producto.setFechaLimite(new Date());
            productoRepo.save(producto);
            return;
        }
        for (Categoria categoria : categoriaRepo.listarPorProducto(idProducto)) {
            categoria.getMisProductos().remove(producto);
            categoriaRepo.save(categoria);
        }
        for (Usuario usuario : usuarioRepo.listarConFavorito(idProducto)) {
            usuario.getMisProductosFavoritos().remove(producto);
            usuarioRepo.save(usuario);
        }
        historialPrecioRepo.deleteAll(historialPrecioRepo.listarPorProducto(idProducto));
        productoRepo.delete(producto);
    }

    @Override
    @Transactional
    public Producto ponerDescuento(Long idProducto, Long idUsuario, Double descuento) throws NegocioException {
        Producto producto = obtenerProducto(idProducto);
        validarDueno(producto, idUsuario, "Solo el vendedor puede modificar el producto");
        if (descuento == null || descuento <= 0 || descuento >= 1) {
            throw new NegocioException("El descuento debe estar entre 0 y 1");
        }
        return cambiarDescuento(producto, descuento);
    }

    @Override
    @Transactional
    public Producto quitarDescuento(Long idProducto, Long idUsuario) throws NegocioException {
        Producto producto = obtenerProducto(idProducto);
        validarDueno(producto, idUsuario, "Solo el vendedor puede modificar el producto");
        return cambiarDescuento(producto, 0.0);
    }

    @Override
    public Double calcularPrecioConDescuento(Producto producto) {
        double descuento = producto.getDescuento() == null ? 0.0 : producto.getDescuento();
        return BigDecimal.valueOf(producto.getPrecio())
                .multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(descuento)))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public List<Producto> listarVigentes() {
        return productoRepo.listarVigentes(new Date());
    }

    @Override
    public List<Producto> listarVigentesPorCategoria(Long idCategoria) throws NegocioException {
        if (idCategoria == null || !categoriaRepo.existsById(idCategoria)) {
            throw new NegocioException("La categoría no existe");
        }
        return productoRepo.listarVigentesPorCategoria(idCategoria, new Date());
    }

    @Override
    public List<Producto> buscar(String texto, Long idCategoria, Double precioMin, Double precioMax,
                                 Long idCiudad, Double calificacionMin) throws NegocioException {
        String filtro = texto == null || texto.trim().isEmpty() ? null : texto.trim();
        if ((precioMin != null && precioMin < 0) || (precioMax != null && precioMax < 0)) {
            throw new NegocioException("Los precios no pueden ser negativos");
        }
        if (precioMin != null && precioMax != null && precioMin > precioMax) {
            throw new NegocioException("El precio mínimo no puede ser mayor que el máximo");
        }
        if (calificacionMin != null && (calificacionMin < 0 || calificacionMin > 5)) {
            throw new NegocioException("La calificación mínima debe estar entre 0 y 5");
        }
        return productoRepo.buscar(filtro, idCategoria, precioMin, precioMax, idCiudad, calificacionMin,
                new Date());
    }

    @Override
    public List<Producto> listarPorVendedor(Long idUsuario) throws NegocioException {
        if (idUsuario == null || !usuarioRepo.existsById(idUsuario)) {
            throw new NegocioException("El vendedor no existe");
        }
        return productoRepo.listarPorVendedor(idUsuario);
    }

    @Override
    public List<Producto> listarFavoritos(String email) {
        return productoRepo.listarFavoritos(email);
    }

    @Override
    public List<Producto> listarFavoritos(Long idUsuario) throws NegocioException {
        cargarUsuario(idUsuario);
        return productoRepo.listarFavoritosPorUsuario(idUsuario);
    }

    @Override
    @Transactional
    public void agregarFavorito(Long idProducto, Long idUsuario) throws NegocioException {
        Usuario usuario = cargarUsuario(idUsuario);
        Producto producto = obtenerProducto(idProducto);
        if (idUsuario.equals(producto.getUsuario().getId())) {
            throw new NegocioException("No puede agregar su propio producto a favoritos");
        }
        if (producto.getFechaLimite() == null || !producto.getFechaLimite().after(new Date())) {
            throw new NegocioException("El producto no está disponible");
        }
        if (usuario.getMisProductosFavoritos() == null) {
            usuario.setMisProductosFavoritos(new ArrayList<>());
        }
        if (usuario.getMisProductosFavoritos().contains(producto)) {
            return;
        }
        usuario.getMisProductosFavoritos().add(producto);
        usuarioRepo.save(usuario);
    }

    @Override
    @Transactional
    public void quitarFavorito(Long idProducto, Long idUsuario) throws NegocioException {
        Usuario usuario = cargarUsuario(idUsuario);
        Producto producto = obtenerProducto(idProducto);
        if (usuario.getMisProductosFavoritos() != null && usuario.getMisProductosFavoritos().remove(producto)) {
            usuarioRepo.save(usuario);
        }
    }

    @Override
    public boolean esFavorito(Long idProducto, Long idUsuario) {
        if (idProducto == null || idUsuario == null) {
            return false;
        }
        return productoRepo.esFavorito(idProducto, idUsuario);
    }

    private Usuario cargarUsuario(Long idUsuario) throws NegocioException {
        if (idUsuario == null) {
            throw new NegocioException("El usuario no existe");
        }
        return usuarioRepo.findById(idUsuario).orElseThrow(() -> new NegocioException("El usuario no existe"));
    }

    @Override
    public Producto obtenerProducto(Long id) throws NegocioException {
        if (id == null) {
            throw new NoEncontradoException("El producto no existe");
        }
        return productoRepo.findById(id)
                .orElseThrow(() -> new NoEncontradoException("El producto no existe"));
    }

    @Override
    public List<Producto> listarTodos() {
        return productoRepo.findAll();
    }

    // ===== Utilidades privadas =====

    private Ciudad validarCiudad(Ciudad ciudad) throws NegocioException {
        if (ciudad == null || ciudad.getId() == null) {
            throw new NegocioException("La ciudad es obligatoria");
        }
        return ciudadRepo.findById(ciudad.getId())
                .orElseThrow(() -> new NegocioException("La ciudad no existe"));
    }

    private void validarImagenes(Producto producto) throws NegocioException {
        if (producto.getMisImagenes() == null || producto.getMisImagenes().isEmpty()) {
            throw new NegocioException("Debe subir al menos una imagen");
        }
    }

    private void validarPrecio(Double precio) throws NegocioException {
        if (precio == null || precio <= 0) {
            throw new NegocioException("El precio debe ser mayor que cero");
        }
    }

    private Double validarDescuentoNuevo(Double descuento) throws NegocioException {
        if (descuento == null) {
            return 0.0;
        }
        if (descuento < 0 || descuento >= 1) {
            throw new NegocioException("El descuento debe estar entre 0 y 1");
        }
        return descuento;
    }

    private void validarDueno(Producto producto, Long idUsuario, String mensaje) throws NegocioException {
        if (idUsuario == null || !idUsuario.equals(producto.getUsuario().getId())) {
            throw new NegocioException(mensaje);
        }
    }

    private Double descuentoDe(Producto producto) {
        return producto.getDescuento() == null ? 0.0 : producto.getDescuento();
    }

    /** Carga las categorías por id; lanza si alguna no existe o está repetida. */
    private List<Categoria> cargarCategorias(List<Long> ids) throws NegocioException {
        List<Categoria> categorias = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            throw new NegocioException("Debe elegir al menos una categoría");
        }
        Set<Long> vistos = new HashSet<>();
        for (Long id : ids) {
            if (id == null || !vistos.add(id)) {
                throw new NegocioException("Hay categorías repetidas o vacías");
            }
            categorias.add(categoriaRepo.findById(id)
                    .orElseThrow(() -> new NegocioException("La categoría " + id + " no existe")));
        }
        return categorias;
    }

    private void agregarACategoria(Categoria categoria, Producto producto) {
        if (categoria.getMisProductos() == null) {
            categoria.setMisProductos(new ArrayList<>());
        }
        if (!categoria.getMisProductos().contains(producto)) {
            categoria.getMisProductos().add(producto);
            categoriaRepo.save(categoria);
        }
    }

    private void sincronizarCategorias(Producto producto, List<Categoria> nuevas) {
        Set<Long> idsNuevas = new HashSet<>();
        for (Categoria c : nuevas) {
            idsNuevas.add(c.getId());
        }
        for (Categoria actual : categoriaRepo.listarPorProducto(producto.getId())) {
            if (!idsNuevas.contains(actual.getId())) {
                actual.getMisProductos().remove(producto);
                categoriaRepo.save(actual);
            }
        }
        for (Categoria c : nuevas) {
            agregarACategoria(c, producto);
        }
    }

    private Producto cambiarDescuento(Producto producto, Double descuentoNuevo) {
        Double precioAnterior = producto.getPrecio();
        Double descuentoAnterior = descuentoDe(producto);
        producto.setDescuento(descuentoNuevo);
        Producto guardado = productoRepo.save(producto);
        registrarCambioDePrecio(guardado, precioAnterior, descuentoAnterior);
        return guardado;
    }

    /** Guarda un historial si el precio o el descuento cambiaron respecto a los valores anteriores. */
    private void registrarCambioDePrecio(Producto producto, Double precioAnterior, Double descuentoAnterior) {
        Double descuentoNuevo = descuentoDe(producto);
        if (Objects.equals(precioAnterior, producto.getPrecio()) && Objects.equals(descuentoAnterior, descuentoNuevo)) {
            return;
        }
        HistorialPrecio historial = new HistorialPrecio();
        historial.setProducto(producto);
        historial.setPrecioAnterior(precioAnterior);
        historial.setPrecioNuevo(producto.getPrecio());
        historial.setDescuentoAnterior(descuentoAnterior);
        historial.setDescuentoNuevo(descuentoNuevo);
        historial.setFecha(new Date());
        historialPrecioRepo.save(historial);
    }

    private boolean tieneDependientes(Long idProducto) {
        return detalleCompraRepo.existsByProductoId(idProducto)
                || subastaRepo.existsByProductoId(idProducto)
                || comentarioRepo.existsByProductoId(idProducto)
                || chatRepo.existsByProductoId(idProducto)
                || reporteProductoRepo.existsByProductoId(idProducto);
    }
}
