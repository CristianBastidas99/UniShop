package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.repositorios.DetalleCompraRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetalleCompraServicioImpl implements DetalleCompraServicio{

    private final DetalleCompraRepo detalleCompraRepo;

    public DetalleCompraServicioImpl(DetalleCompraRepo detalleCompraRepo) {
        this.detalleCompraRepo = detalleCompraRepo;
    }


    @Override
    public DetalleCompra registrarDetalleCompra(DetalleCompra dc) throws Exception {
        Optional<DetalleCompra> buscado = detalleCompraRepo.findById(dc.getId());
        if (buscado.isPresent()) {
            throw new Exception("Ya se encuentra registrada");
        }
        return detalleCompraRepo.save(dc);
    }

    @Override
    public DetalleCompra actualizarDetalleCompra(DetalleCompra dc) throws Exception {
        Optional<DetalleCompra> buscado = detalleCompraRepo.findById(dc.getId());
        if (!buscado.isPresent()) {
            throw new Exception("La compra no existe");
        }
        return detalleCompraRepo.save(dc);
    }

    @Override
    public DetalleCompra obtenerDetalleCompra(Long id) throws Exception {
        Optional<DetalleCompra> buscado = detalleCompraRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("La compra que busca no existe");
        }
        return buscado.get();
    }

    @Override
    public void eliminarDetalleCompra(Long id) throws Exception {
        Optional<DetalleCompra> buscado = detalleCompraRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("La compra no existe");
        }
        detalleCompraRepo.delete(buscado.get());
    }

    @Override
    public List<DetalleCompra> listarDetalleCompra() {
        return detalleCompraRepo.findAll();
    }
}
