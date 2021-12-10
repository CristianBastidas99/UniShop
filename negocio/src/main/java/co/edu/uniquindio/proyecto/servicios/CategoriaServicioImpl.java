package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServicioImpl implements CategoriaServicio {

    private final CategoriaRepo categoriaRepo;


    public CategoriaServicioImpl(CategoriaRepo categoriaRepo) {
        this.categoriaRepo = categoriaRepo;
    }

    @Override
    public Categoria registrarCategoria(Categoria c) throws Exception {
        Optional<Categoria> buscado = categoriaRepo.findById(c.getId());
        if (buscado.isPresent()) {
            throw new Exception("El correo ya se encuentra registrado");
        }
        return categoriaRepo.save(c);
    }

    @Override
    public Categoria actualizarCategoria(Categoria c) throws Exception {
        Optional<Categoria> buscado = categoriaRepo.findById(c.getId());
        if (!buscado.isPresent()) {
            throw new Exception("El administrador no existe");
        }
        return categoriaRepo.save(c);
    }

    @Override
    public Categoria obtenerCategoria(Long id) throws Exception {
        Optional<Categoria> buscado = categoriaRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("El administrador que busca no existe");
        }
        return buscado.get();
    }

    @Override
    public void eliminarCategoria(Long id) throws Exception {
        Optional<Categoria> buscado = categoriaRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("El administrador no existe");
        }
        categoriaRepo.delete(buscado.get());
    }

    @Override
    public List<Categoria> listarCategoria() {
        return categoriaRepo.findAll();
    }
}
