package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Chat;

import java.util.List;

public interface ChatServicio {

    Chat registrarChat(Chat c) throws Exception;
    Chat actualizarChat(Chat c) throws Exception;
    Chat obtenerChat(Long id) throws Exception;
    void eliminarChat(Long id) throws Exception;
    List<Chat> listarChat();
    List<Chat> listarChatPersona(Long id);

}
