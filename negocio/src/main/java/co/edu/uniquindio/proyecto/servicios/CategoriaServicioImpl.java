package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServicioImpl implements CategoriaServicio {

    private final CategoriaRepo categoriaRepo;

    public CategoriaServicioImpl(CategoriaRepo categoriaRepo) {
        this.categoriaRepo = categoriaRepo;
    }

    @Override
    public Categoria registrarCategoria(Categoria c) throws NegocioException {
        if (c.getId() != null && categoriaRepo.existsById(c.getId())) {
            throw new NegocioException("La categoría ya existe");
        }
        if (categoriaRepo.findByNombre(c.getNombre()).isPresent()) {
            throw new NegocioException("La categoría " + c.getNombre() + " ya existe");
        }
        return categoriaRepo.save(c);
    }

    @Override
    public Categoria actualizarCategoria(Categoria c) throws NegocioException {
        if (c.getId() == null || !categoriaRepo.existsById(c.getId())) {
            throw new NegocioException("La categoría no existe");
        }
        boolean nombreDeOtra = categoriaRepo.findByNombre(c.getNombre())
                .map(otra -> !otra.getId().equals(c.getId()))
                .orElse(false);
        if (nombreDeOtra) {
            throw new NegocioException("La categoría " + c.getNombre() + " ya existe");
        }
        return categoriaRepo.save(c);
    }

    @Override
    public Categoria obtenerCategoria(Long id) throws NegocioException {
        return categoriaRepo.findById(id)
                .orElseThrow(() -> new NegocioException("La categoría no existe"));
    }

    @Override
    public void eliminarCategoria(Long id) throws NegocioException {
        categoriaRepo.delete(obtenerCategoria(id));
    }

    @Override
    public List<Categoria> listarCategoria() {
        return categoriaRepo.findAll();
    }
}
