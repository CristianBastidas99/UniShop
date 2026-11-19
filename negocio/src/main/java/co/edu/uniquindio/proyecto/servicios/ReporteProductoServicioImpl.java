package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.ReporteProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReporteProductoServicioImpl implements ReporteProductoServicio {

    private final ReporteProductoRepo reporteProductoRepo;
    private final UsuarioRepo usuarioRepo;
    private final ProductoRepo productoRepo;
    private final AdministradorRepo administradorRepo;

    public ReporteProductoServicioImpl(ReporteProductoRepo reporteProductoRepo, UsuarioRepo usuarioRepo,
                                       ProductoRepo productoRepo, AdministradorRepo administradorRepo) {
        this.reporteProductoRepo = reporteProductoRepo;
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
        this.administradorRepo = administradorRepo;
    }

    @Override
    public ReporteProducto registrarReporte(ReporteProducto r) throws NegocioException {
        if (r.getId() != null && reporteProductoRepo.existsById(r.getId())) {
            throw new NegocioException("El reporte ya existe");
        }
        return reporteProductoRepo.save(r);
    }

    @Override
    public ReporteProducto actualizarReporte(ReporteProducto r) throws NegocioException {
        if (r.getId() == null || !reporteProductoRepo.existsById(r.getId())) {
            throw new NegocioException("El reporte no existe");
        }
        return reporteProductoRepo.save(r);
    }

    @Override
    public ReporteProducto obtenerReporte(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("El reporte no existe");
        }
        return reporteProductoRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El reporte no existe"));
    }

    @Override
    public void eliminarReporte(Long id) throws NegocioException {
        reporteProductoRepo.delete(obtenerReporte(id));
    }

    @Override
    public List<ReporteProducto> listarReportes() {
        return reporteProductoRepo.findAll();
    }

    @Override
    public ReporteProducto reportarProducto(ReporteProducto reporte) throws NegocioException {
        if (reporte.getUsuario() == null || reporte.getUsuario().getId() == null) {
            throw new NegocioException("El usuario es obligatorio");
        }
        if (reporte.getProducto() == null || reporte.getProducto().getId() == null) {
            throw new NegocioException("El producto es obligatorio");
        }
        Usuario usuario = usuarioRepo.findById(reporte.getUsuario().getId())
                .orElseThrow(() -> new NegocioException("El usuario no existe"));
        Producto producto = productoRepo.findById(reporte.getProducto().getId())
                .orElseThrow(() -> new NegocioException("El producto no existe"));
        if (producto.getUsuario() != null && producto.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("No puede reportar su propio producto");
        }
        if (reporteProductoRepo.existsByUsuarioIdAndProductoId(usuario.getId(), producto.getId())) {
            throw new NegocioException("Ya reportó este producto");
        }
        reporte.setId(null);
        reporte.setUsuario(usuario);
        reporte.setProducto(producto);
        reporte.setFecha(new Date());
        reporte.setEstado(EstadoReporte.PENDIENTE);
        return reporteProductoRepo.save(reporte);
    }

    @Override
    public List<ReporteProducto> listarPendientes() {
        return reporteProductoRepo.listarPorEstado(EstadoReporte.PENDIENTE);
    }

    @Override
    public boolean yaReporto(Long idUsuario, Long idProducto) {
        return idUsuario != null && idProducto != null
                && reporteProductoRepo.existsByUsuarioIdAndProductoId(idUsuario, idProducto);
    }

    @Override
    public List<ReporteProducto> listarPorProducto(Long idProducto) {
        return reporteProductoRepo.listarPorProducto(idProducto);
    }

    @Override
    public ReporteProducto cambiarEstado(Long idReporte, EstadoReporte nuevoEstado, String respuesta,
                                         String emailAdministrador) throws NegocioException {
        ReporteProducto reporte = obtenerReporte(idReporte);
        if (reporte.getEstado() == EstadoReporte.ACEPTADO || reporte.getEstado() == EstadoReporte.RECHAZADO) {
            throw new NegocioException("El reporte ya fue resuelto");
        }
        Administrador administrador = emailAdministrador == null ? null
                : administradorRepo.findByEmail(emailAdministrador).orElse(null);
        if (administrador == null) {
            throw new NegocioException("El administrador no existe");
        }
        if (nuevoEstado == null || nuevoEstado == EstadoReporte.PENDIENTE) {
            throw new NegocioException("No se puede volver a PENDIENTE");
        }
        if ((nuevoEstado == EstadoReporte.ACEPTADO || nuevoEstado == EstadoReporte.RECHAZADO)
                && (respuesta == null || respuesta.trim().isEmpty())) {
            throw new NegocioException("La respuesta es obligatoria");
        }
        reporte.setEstado(nuevoEstado);
        reporte.setRespuestaAdministrador(respuesta);
        reporte.setAdministrador(administrador);
        return reporteProductoRepo.save(reporte);
    }
}
