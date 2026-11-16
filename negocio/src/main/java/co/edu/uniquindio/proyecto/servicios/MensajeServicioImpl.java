package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.MensajeRepo;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MensajeServicioImpl implements MensajeServicio {

    private final MensajeRepo mensajeRepo;

    public MensajeServicioImpl(MensajeRepo mensajeRepo) {
        this.mensajeRepo = mensajeRepo;
    }

    @Override
    public Mensaje registrarMensaje(Mensaje mensaje) throws NegocioException {
        if (mensaje.getId() != null && mensajeRepo.existsById(mensaje.getId())) {
            throw new NegocioException("El mensaje ya existe");
        }
        return mensajeRepo.save(mensaje);
    }

    @Override
    public Mensaje actualizarMensaje(Mensaje mensaje) throws NegocioException {
        if (mensaje.getId() == null || !mensajeRepo.existsById(mensaje.getId())) {
            throw new NegocioException("El mensaje no existe");
        }
        return mensajeRepo.save(mensaje);
    }

    @Override
    public Mensaje obtenerMensaje(Long id) throws NegocioException {
        return mensajeRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El mensaje no existe"));
    }

    @Override
    public void eliminarMensaje(Long id) throws NegocioException {
        Mensaje buscado = obtenerMensaje(id);
        mensajeRepo.delete(buscado);
    }

    @Override
    public List<Mensaje> listarMensaje() {
        return mensajeRepo.findAll();
    }

    @Override
    public List<Mensaje> listarMensajeChat(Long idChat) {
        return mensajeRepo.listarPorChat(idChat);
    }
}
