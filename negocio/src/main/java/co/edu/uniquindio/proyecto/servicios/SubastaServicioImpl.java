package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaUsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SubastaServicioImpl implements SubastaServicio {

    private final SubastaRepo subastaRepo;
    private final SubastaUsuarioRepo subastaUsuarioRepo;

    public SubastaServicioImpl(SubastaRepo subastaRepo, SubastaUsuarioRepo subastaUsuarioRepo) {
        this.subastaRepo = subastaRepo;
        this.subastaUsuarioRepo = subastaUsuarioRepo;
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
}
