package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompraServicioImpl implements CompraServicio {

    private final CompraRepo compraRepo;

    public CompraServicioImpl(CompraRepo compraRepo) {
        this.compraRepo = compraRepo;
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

}
