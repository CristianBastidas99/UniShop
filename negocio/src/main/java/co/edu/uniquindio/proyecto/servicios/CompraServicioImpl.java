package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompraServicioImpl implements CompraServicio{

    private final CompraRepo compraRepo;

    public CompraServicioImpl(CompraRepo compraRepo) {
        this.compraRepo = compraRepo;
    }


    @Override
    public Compra registrarCompra(Compra c) throws Exception {
        Optional<Compra> buscado = compraRepo.findById(c.getId());
        if (buscado.isPresent()) {
            throw new Exception("La compra ya se encuentra registrada");
        }
        return compraRepo.save(c);
    }

    @Override
    public Compra actualizarCompra(Compra c) throws Exception {
        Optional<Compra> buscado = compraRepo.findById(c.getId());
        if (!buscado.isPresent()) {
            throw new Exception("La compra no existe");
        }
        return compraRepo.save(c);
    }

    @Override
    public Compra obtenerCompra(Long id) throws Exception {
        Optional<Compra> buscado = compraRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("La compra que busca no existe");
        }
        return buscado.get();
    }

    @Override
    public void eliminarCompra(Long id) throws Exception {
        Optional<Compra> buscado = compraRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("La compra no existe");
        }
        compraRepo.delete(buscado.get());
    }

    @Override
    public List<Compra> listarCompra() {
        return compraRepo.findAll();
    }
}
