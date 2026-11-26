package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaUsuarioRepo;
import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class SubastaUsuarioServicioImpl implements SubastaUsuarioServicio {

    private final SubastaUsuarioRepo subastaUsuarioRepo;
    private final SubastaRepo subastaRepo;

    private final UsuarioRepo usuarioRepo;

    public SubastaUsuarioServicioImpl(SubastaUsuarioRepo subastaUsuarioRepo, SubastaRepo subastaRepo,
                                      UsuarioRepo usuarioRepo) {
        this.subastaUsuarioRepo = subastaUsuarioRepo;
        this.subastaRepo = subastaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    @Transactional(rollbackFor = NegocioException.class)
    public SubastaUsuario pujar(Long idSubasta, Long idUsuario, double valor) throws NegocioException {
        if (idSubasta == null) {
            throw new NegocioException("La subasta no existe");
        }
        Subasta subasta = subastaRepo.findById(idSubasta)
                .orElseThrow(() -> new NegocioException("La subasta no existe"));
        Date ahora = new Date();
        if (subasta.isCerrada() || !subasta.getFechaLimite().after(ahora)) {
            throw new NegocioException("La subasta no está activa");
        }
        if (!subasta.getProducto().getUsuario().isActivo()) {
            throw new NegocioException("La subasta no está disponible");
        }
        if (idUsuario == null) {
            throw new NegocioException("El usuario no existe");
        }
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new NegocioException("El usuario no existe"));
        if (idUsuario.equals(subasta.getProducto().getUsuario().getId())) {
            throw new NegocioException("El vendedor no puede pujar en su propia subasta");
        }
        Double maxima = subastaUsuarioRepo.pujaMasAlta(idSubasta);
        double minima = maxima == null ? 0.0 : maxima;
        if (valor <= minima) {
            throw new NegocioException("La puja debe ser mayor a $" + (long) minima);
        }
        SubastaUsuario puja = new SubastaUsuario();
        puja.setSubasta(subasta);
        puja.setUsuario(usuario);
        puja.setValor(valor);
        puja.setFechaSubasta(ahora);
        return subastaUsuarioRepo.save(puja);
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
