package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CiudadRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CiudadServicioImpl implements CiudadServicio {

    private final CiudadRepo ciudadRepo;
    private final UsuarioRepo usuarioRepo;
    private final ProductoRepo productoRepo;

    public CiudadServicioImpl(CiudadRepo ciudadRepo, UsuarioRepo usuarioRepo, ProductoRepo productoRepo) {
        this.ciudadRepo = ciudadRepo;
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
    }

    @Override
    public Ciudad registrarCiudad(Ciudad c) throws NegocioException {
        if (c.getId() != null && ciudadRepo.existsById(c.getId())) {
            throw new NegocioException("La ciudad ya existe");
        }
        if (ciudadRepo.findByNombre(c.getNombre()).isPresent()) {
            throw new NegocioException("La ciudad " + c.getNombre() + " ya existe");
        }
        return ciudadRepo.save(c);
    }

    @Override
    public Ciudad actualizarCiudad(Ciudad c) throws NegocioException {
        if (c.getId() == null || !ciudadRepo.existsById(c.getId())) {
            throw new NegocioException("La ciudad no existe");
        }
        boolean nombreDeOtra = ciudadRepo.findByNombre(c.getNombre())
                .map(otra -> !otra.getId().equals(c.getId()))
                .orElse(false);
        if (nombreDeOtra) {
            throw new NegocioException("La ciudad " + c.getNombre() + " ya existe");
        }
        return ciudadRepo.save(c);
    }

    @Override
    public Ciudad obtenerCiudad(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("La ciudad no existe");
        }
        return ciudadRepo.findById(id)
                .orElseThrow(() -> new NegocioException("La ciudad no existe"));
    }

    @Override
    public void eliminarCiudad(Long id) throws NegocioException {
        Ciudad ciudad = obtenerCiudad(id);
        if (usuarioRepo.existsByCiudadId(id) || productoRepo.existsByCiudadId(id)) {
            throw new NegocioException("La ciudad tiene usuarios o productos asociados y no se puede eliminar");
        }
        ciudadRepo.delete(ciudad);
    }

    @Override
    public List<Ciudad> listarCiudades() {
        return ciudadRepo.findAll();
    }
}
