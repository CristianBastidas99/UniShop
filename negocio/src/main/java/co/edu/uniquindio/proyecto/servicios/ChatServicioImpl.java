package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatServicioImpl implements ChatServicio{

    private final ChatRepo chatRepo;
    public ChatServicioImpl(ChatRepo chatRepo) {
        this.chatRepo = chatRepo;
    }


    @Override
    public Chat registrarChat(Chat c) throws Exception {
        Optional<Chat> buscado = chatRepo.findById(c.getId());
        if (buscado.isPresent()) {
            throw new Exception("El chat ya se encuentra registrado");
        }
        return chatRepo.save(c);
    }

    @Override
    public Chat actualizarChat(Chat c) throws Exception {
        Optional<Chat> buscado = chatRepo.findById(c.getId());
        if (!buscado.isPresent()) {
            throw new Exception("Se produjo un error y el chat no se encontro");
        }
        return chatRepo.save(c);
    }

    @Override
    public Chat obtenerChat(Long id) throws Exception {
        Optional<Chat> buscado = chatRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("Un chat con ese id no existe");
        }
        return buscado.get();
    }

    @Override
    public void eliminarChat(Long id) throws Exception {
        Optional<Chat> buscado = chatRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("El chat que quiere eliminar no existe en la base de datos");
        }
        chatRepo.delete(buscado.get());
    }

    @Override
    public List<Chat> listarChat() {
        return chatRepo.findAll();
    }

    @Override
    public List<Chat> listarChatPersona(Long id) {
        return chatRepo.FindAllWithDescriptionQuery(id);
    }
}
