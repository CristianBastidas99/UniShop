package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaUsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubastaUsuarioServicioImpl implements SubastaUsuarioServicio {

    private final SubastaUsuarioRepo subastaUsuarioRepo;
    private final SubastaRepo subastaRepo;

    public SubastaUsuarioServicioImpl(SubastaUsuarioRepo subastaUsuarioRepo, SubastaRepo subastaRepo) {
        this.subastaUsuarioRepo = subastaUsuarioRepo;
        this.subastaRepo = subastaRepo;
    }

    @Override
    public SubastaUsuario registrarPuja(SubastaUsuario p) throws NegocioException {
        if (p.getId() != null && subastaUsuarioRepo.existsById(p.getId())) {
            throw new NegocioException("La puja ya existe");
        }
        return subastaUsuarioRepo.save(p);
    }

    @Override
    public SubastaUsuario actualizarPuja(SubastaUsuario p) throws NegocioException {
        if (p.getId() == null || !subastaUsuarioRepo.existsById(p.getId())) {
            throw new NegocioException("La puja no existe");
        }
        return subastaUsuarioRepo.save(p);
    }

    @Override
    public SubastaUsuario obtenerPuja(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("La puja no existe");
        }
        return subastaUsuarioRepo.findById(id)
                .orElseThrow(() -> new NegocioException("La puja no existe"));
    }

    @Override
    public void eliminarPuja(Long id) throws NegocioException {
        subastaUsuarioRepo.delete(obtenerPuja(id));
    }

    @Override
    public List<SubastaUsuario> listarPujas() {
        return subastaUsuarioRepo.findAll();
    }

    @Override
    public List<SubastaUsuario> listarPorSubasta(Long idSubasta) throws NegocioException {
        validarSubasta(idSubasta);
        return subastaUsuarioRepo.listarPorSubasta(idSubasta);
    }

    @Override
    public Double obtenerPujaMasAlta(Long idSubasta) throws NegocioException {
        validarSubasta(idSubasta);
        Double maxima = subastaUsuarioRepo.pujaMasAlta(idSubasta);
        return maxima == null ? 0.0 : maxima;
    }

    private void validarSubasta(Long idSubasta) throws NegocioException {
        if (idSubasta == null || !subastaRepo.existsById(idSubasta)) {
            throw new NegocioException("La subasta no existe");
        }
    }
}
