package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.DetalleCompraRepo;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DetalleCompraServicioImpl implements DetalleCompraServicio {

    private final DetalleCompraRepo detalleCompraRepo;

    public DetalleCompraServicioImpl(DetalleCompraRepo detalleCompraRepo) {
        this.detalleCompraRepo = detalleCompraRepo;
    }

    @Override
    public DetalleCompra registrarDetalleCompra(DetalleCompra detalleCompra) throws NegocioException {
        if (detalleCompra.getId() != null && detalleCompraRepo.existsById(detalleCompra.getId())) {
            throw new NegocioException("El detalle de compra ya existe");
        }
        return detalleCompraRepo.save(detalleCompra);
    }

    @Override
    public DetalleCompra actualizarDetalleCompra(DetalleCompra detalleCompra) throws NegocioException {
        if (detalleCompra.getId() == null || !detalleCompraRepo.existsById(detalleCompra.getId())) {
            throw new NegocioException("El detalle de compra no existe");
        }
        return detalleCompraRepo.save(detalleCompra);
    }

    @Override
    public DetalleCompra obtenerDetalleCompra(Long id) throws NegocioException {
        return detalleCompraRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El detalle de compra no existe"));
    }

    @Override
    public void eliminarDetalleCompra(Long id) throws NegocioException {
        DetalleCompra buscado = obtenerDetalleCompra(id);
        detalleCompraRepo.delete(buscado);
    }

    @Override
    public List<DetalleCompra> listarDetalleCompra() {
        return detalleCompraRepo.findAll();
    }

}
