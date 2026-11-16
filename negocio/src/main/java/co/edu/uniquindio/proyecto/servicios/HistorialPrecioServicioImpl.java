package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.HistorialPrecio;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.HistorialPrecioRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HistorialPrecioServicioImpl implements HistorialPrecioServicio {

    private final HistorialPrecioRepo historialPrecioRepo;
    private final ProductoRepo productoRepo;

    public HistorialPrecioServicioImpl(HistorialPrecioRepo historialPrecioRepo, ProductoRepo productoRepo) {
        this.historialPrecioRepo = historialPrecioRepo;
        this.productoRepo = productoRepo;
    }

    @Override
    public HistorialPrecio registrarHistorial(HistorialPrecio h) throws NegocioException {
        if (h.getId() != null && historialPrecioRepo.existsById(h.getId())) {
            throw new NegocioException("El historial de precio ya existe");
        }
        if (h.getFecha() == null) {
            h.setFecha(new Date());
        }
        return historialPrecioRepo.save(h);
    }

    @Override
    public HistorialPrecio actualizarHistorial(HistorialPrecio h) throws NegocioException {
        if (h.getId() == null || !historialPrecioRepo.existsById(h.getId())) {
            throw new NegocioException("El historial de precio no existe");
        }
        return historialPrecioRepo.save(h);
    }

    @Override
    public HistorialPrecio obtenerHistorial(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("El historial de precio no existe");
        }
        return historialPrecioRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El historial de precio no existe"));
    }

    @Override
    public void eliminarHistorial(Long id) throws NegocioException {
        historialPrecioRepo.delete(obtenerHistorial(id));
    }

    @Override
    public List<HistorialPrecio> listarHistoriales() {
        return historialPrecioRepo.findAll();
    }

    @Override
    public List<HistorialPrecio> listarPorProducto(Long idProducto) throws NegocioException {
        if (idProducto == null || !productoRepo.existsById(idProducto)) {
            throw new NegocioException("El producto no existe");
        }
        return historialPrecioRepo.listarPorProducto(idProducto);
    }
}
