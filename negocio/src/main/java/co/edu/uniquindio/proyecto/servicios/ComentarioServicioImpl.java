package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComentarioServicioImpl implements  ComentarioServicio{

    private final ComentarioRepo comentarioRepo;

    public ComentarioServicioImpl(ComentarioRepo comentarioRepo) {
        this.comentarioRepo = comentarioRepo;
    }


    @Override
    public Comentario registrarComentario(Comentario c) throws Exception {
        Optional<Comentario> buscado = comentarioRepo.findById(c.getId());
        if (buscado.isPresent()) {
            throw new Exception("El producto ya se encuentra registrado");
        }
        return comentarioRepo.save(c);
    }

    @Override
    public Comentario actualizarComentario(Comentario c) throws Exception {
        Optional<Comentario> buscado = comentarioRepo.findById(c.getId());
        if (!buscado.isPresent()) {
            throw new Exception("Se produjo un error y el producto no se encontro");
        }
        return comentarioRepo.save(c);
    }

    @Override
    public Comentario obtenerComentario(Long id) throws Exception {
        Optional<Comentario> buscado = comentarioRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("Un producto con ese id no existe");
        }
        return buscado.get();
    }

    @Override
    public void eliminarComentario(Long id) throws Exception {
        Optional<Comentario> buscado = comentarioRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("El producto que quiere eliminar no existe en la base de datos");
        }
        comentarioRepo.delete(buscado.get());
    }

    @Override
    public List<Comentario> listarComentario() {
        return comentarioRepo.findAll();
    }

    @Override
    public List<Comentario> listarComentarioProducto(Long idProducto) {
        return comentarioRepo.FindAllWithDescriptionQueryProducto(idProducto);
    }

    @Override
    public List<Comentario> listarComentarioUsuario(Long idUsuario) {
        return comentarioRepo.FindAllWithDescriptionQueryUsuario(idUsuario);
    }
}
