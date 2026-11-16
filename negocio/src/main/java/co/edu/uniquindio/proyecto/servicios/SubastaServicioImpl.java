package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaUsuarioRepo;
import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SubastaServicioImpl implements SubastaServicio {

    private final SubastaRepo subastaRepo;
    private final SubastaUsuarioRepo subastaUsuarioRepo;
    private final ProductoRepo productoRepo;
    private final CompraServicio compraServicio;

    public SubastaServicioImpl(SubastaRepo subastaRepo, SubastaUsuarioRepo subastaUsuarioRepo,
                               ProductoRepo productoRepo, CompraServicio compraServicio) {
        this.subastaRepo = subastaRepo;
        this.subastaUsuarioRepo = subastaUsuarioRepo;
        this.productoRepo = productoRepo;
        this.compraServicio = compraServicio;
    }

    @Override
    public Subasta registrarSubasta(Subasta s) throws NegocioException {
        if (s.getId() != null && subastaRepo.existsById(s.getId())) {
            throw new NegocioException("La subasta ya existe");
        }
        return subastaRepo.save(s);
    }

    @Override
    public Subasta actualizarSubasta(Subasta s) throws NegocioException {
        if (s.getId() == null || !subastaRepo.existsById(s.getId())) {
            throw new NegocioException("La subasta no existe");
        }
        return subastaRepo.save(s);
    }

    @Override
    public Subasta obtenerSubasta(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("La subasta no existe");
        }
        return subastaRepo.findById(id)
                .orElseThrow(() -> new NegocioException("La subasta no existe"));
    }

    @Override
    public void eliminarSubasta(Long id) throws NegocioException {
        Subasta subasta = obtenerSubasta(id);
        if (subastaUsuarioRepo.existsBySubastaId(id)) {
            throw new NegocioException("La subasta tiene pujas y no se puede eliminar");
        }
        subastaRepo.delete(subasta);
    }

    @Override
    public List<Subasta> listarSubastas() {
        return subastaRepo.findAll();
    }

    @Override
    public List<Subasta> listarActivas() {
        return subastaRepo.listarActivas(new Date());
    }

    @Override
    public List<Subasta> listarVencidasSinCerrar() {
        return subastaRepo.listarVencidasSinCerrar(new Date());
    }

    @Override
    @Transactional(rollbackFor = NegocioException.class)
    public Subasta crearSubasta(Long idProducto, Long idVendedor, int horas) throws NegocioException {
        if (idProducto == null) {
            throw new NegocioException("El producto no existe");
        }
        Producto producto = productoRepo.findById(idProducto)
                .orElseThrow(() -> new NegocioException("El producto no existe"));
        if (idVendedor == null || !idVendedor.equals(producto.getUsuario().getId())) {
            throw new NegocioException("Solo el vendedor puede subastar su producto");
        }
        Date ahora = new Date();
        if (producto.getFechaLimite() == null || !producto.getFechaLimite().after(ahora)) {
            throw new NegocioException("El producto no está vigente");
        }
        if (producto.getUnidades() <= 0) {
            throw new NegocioException("El producto no tiene unidades disponibles");
        }
        if (horas <= 0) {
            throw new NegocioException("La duración debe ser de al menos una hora");
        }
        Date fechaLimite = new Date(ahora.getTime() + horas * 3_600_000L);
        if (fechaLimite.after(producto.getFechaLimite())) {
            throw new NegocioException("La subasta no puede terminar después de la fecha límite del producto");
        }
        if (subastaRepo.existeActivaPorProducto(idProducto, ahora)) {
            throw new NegocioException("El producto ya tiene una subasta activa");
        }
        Subasta subasta = new Subasta();
        subasta.setProducto(producto);
        subasta.setFechaLimite(fechaLimite);
        subasta.setCerrada(false);
        return subastaRepo.save(subasta);
    }

    @Override
    @Transactional(rollbackFor = NegocioException.class)
    public Subasta cerrarSubasta(Long idSubasta) throws NegocioException {
        Subasta subasta = obtenerSubasta(idSubasta);
        if (subasta.isCerrada()) {
            throw new NegocioException("La subasta ya está cerrada");
        }
        if (subasta.getFechaLimite().after(new Date())) {
            throw new NegocioException("La subasta todavía está activa");
        }
        List<SubastaUsuario> pujas = subastaUsuarioRepo.listarPorSubasta(idSubasta);
        // La primera puja es la ganadora: mayor valor y, en empate, la más antigua
        if (!pujas.isEmpty() && subasta.getProducto().getUnidades() >= 1) {
            SubastaUsuario ganadora = pujas.get(0);
            Compra compra = compraServicio.comprarEnSubasta(ganadora.getUsuario().getId(),
                    subasta.getProducto().getId(), ganadora.getValor());
            subasta.setCompra(compra);
        }
        subasta.setCerrada(true);
        return subastaRepo.save(subasta);
    }
}
