package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ComentarioServicioImpl implements ComentarioServicio {

    private final ComentarioRepo comentarioRepo;

    public ComentarioServicioImpl(ComentarioRepo comentarioRepo) {
        this.comentarioRepo = comentarioRepo;
    }

    @Override
    public Comentario registrarComentario(Comentario comentario) throws NegocioException {
        if (comentario.getId() != null && comentarioRepo.existsById(comentario.getId())) {
            throw new NegocioException("El comentario ya existe");
        }
        return comentarioRepo.save(comentario);
    }

    @Override
    public Comentario actualizarComentario(Comentario comentario) throws NegocioException {
        if (comentario.getId() == null || !comentarioRepo.existsById(comentario.getId())) {
            throw new NegocioException("El comentario no existe");
        }
        return comentarioRepo.save(comentario);
    }

    @Override
    public Comentario obtenerComentario(Long id) throws NegocioException {
        return comentarioRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El comentario no existe"));
    }

    @Override
    public void eliminarComentario(Long id) throws NegocioException {
        Comentario buscado = obtenerComentario(id);
        comentarioRepo.delete(buscado);
    }

    @Override
    public List<Comentario> listarComentario() {
        return comentarioRepo.findAll();
    }

    @Override
    public List<Comentario> listarComentarioProducto(Long idProducto) {
        return comentarioRepo.listarPorProducto(idProducto);
    }

    @Override
    public List<Comentario> listarComentarioUsuario(Long idUsuario) {
        return comentarioRepo.listarPorUsuario(idUsuario);
    }
}
