package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServicioImpl implements ChatServicio {

    private final ChatRepo chatRepo;

    public ChatServicioImpl(ChatRepo chatRepo) {
        this.chatRepo = chatRepo;
    }

    @Override
    public Chat registrarChat(Chat c) throws NegocioException {
        if (c.getId() != null && chatRepo.existsById(c.getId())) {
            throw new NegocioException("El chat ya existe");
        }
        if (c.getUsuario() != null && c.getProducto() != null
                && chatRepo.buscarPorCompradorYProducto(c.getUsuario().getId(), c.getProducto().getId()).isPresent()) {
            throw new NegocioException("El comprador ya tiene un chat sobre este producto");
        }
        return chatRepo.save(c);
    }

    @Override
    public Chat actualizarChat(Chat c) throws NegocioException {
        if (c.getId() == null || !chatRepo.existsById(c.getId())) {
            throw new NegocioException("El chat no existe");
        }
        return chatRepo.save(c);
    }

    @Override
    public Chat obtenerChat(Long id) throws NegocioException {
        return chatRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El chat no existe"));
    }

    @Override
    public void eliminarChat(Long id) throws NegocioException {
        chatRepo.delete(obtenerChat(id));
    }

    @Override
    public List<Chat> listarChat() {
        return chatRepo.findAll();
    }

    @Override
    public List<Chat> listarChatPersona(Long id) {
        return chatRepo.listarPorUsuario(id);
    }
}
