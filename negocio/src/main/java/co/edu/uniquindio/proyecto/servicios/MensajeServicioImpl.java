package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.repositorios.MensajeRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MensajeServicioImpl implements MensajeServicio{

    private final MensajeRepo mensajeRepo;

    public MensajeServicioImpl(MensajeRepo mensajeRepo) {
        this.mensajeRepo = mensajeRepo;
    }

    @Override
    public Mensaje registrarMensaje(Mensaje m) throws Exception {
        Optional<Mensaje> buscado = mensajeRepo.findById(m.getId());
        if (buscado.isPresent()) {
            throw new Exception("El mensaje ya se encuentra registrado");
        }
        return mensajeRepo.save(m);
    }

    @Override
    public Mensaje actualizarMensaje(Mensaje m) throws Exception {
        Optional<Mensaje> buscado = mensajeRepo.findById(m.getId());
        if (!buscado.isPresent()) {
            throw new Exception("Se produjo un error y el mensaje no se encontro");
        }
        return mensajeRepo.save(m);
    }

    @Override
    public Mensaje obtenerMensaje(Long id) throws Exception {
        Optional<Mensaje> buscado = mensajeRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("Un mensaje con ese id no existe");
        }
        return buscado.get();
    }

    @Override
    public void eliminarMensaje(Long id) throws Exception {
        Optional<Mensaje> buscado = mensajeRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("El mensaje que quiere eliminar no existe en la base de datos");
        }
        mensajeRepo.delete(buscado.get());
    }

    @Override
    public List<Mensaje> listarMensaje() {
        return mensajeRepo.findAll();
    }

    @Override
    public List<Mensaje> listarMensajeChat(Long idChat) {
        return mensajeRepo.FindAllWithDescriptionQueryChat(idChat);
    }
}
