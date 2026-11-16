package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.MensajeRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class MensajeServicioImpl implements MensajeServicio {

    private static final String MENSAJE_NO_PARTICIPANTE = "Solo el comprador y el vendedor pueden escribir en este chat";

    private final MensajeRepo mensajeRepo;
    private final ChatRepo chatRepo;
    private final UsuarioRepo usuarioRepo;

    public MensajeServicioImpl(MensajeRepo mensajeRepo, ChatRepo chatRepo, UsuarioRepo usuarioRepo) {
        this.mensajeRepo = mensajeRepo;
        this.chatRepo = chatRepo;
        this.usuarioRepo = usuarioRepo;
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
    @Transactional(rollbackFor = NegocioException.class)
    public Mensaje enviarMensaje(Long idChat, Long idRemitente, String texto) throws NegocioException {
        Chat chat = idChat == null ? null : chatRepo.findById(idChat).orElse(null);
        if (chat == null) {
            throw new NegocioException("El chat no existe");
        }
        Usuario remitente = idRemitente == null ? null : usuarioRepo.findById(idRemitente).orElse(null);
        if (remitente == null) {
            throw new NegocioException("El usuario no existe");
        }
        if (!ChatServicioImpl.esParticipante(chat, idRemitente)) {
            throw new NegocioException(MENSAJE_NO_PARTICIPANTE);
        }
        if (texto == null || texto.trim().isEmpty()) {
            throw new NegocioException("El mensaje es obligatorio");
        }
        String limpio = texto.trim();
        if (limpio.length() > 1000) {
            throw new NegocioException("El mensaje no puede superar los 1000 caracteres");
        }
        Mensaje mensaje = new Mensaje();
        mensaje.setChat(chat);
        mensaje.setRemitente(remitente);
        mensaje.setMensaje(limpio);
        mensaje.setFecha(new Date());
        return mensajeRepo.save(mensaje);
    }

    @Override
    public List<Mensaje> listarMensajes(Long idChat, Long idUsuario) throws NegocioException {
        Chat chat = idChat == null ? null : chatRepo.findById(idChat).orElse(null);
        if (chat == null) {
            throw new NegocioException("El chat no existe");
        }
        if (!ChatServicioImpl.esParticipante(chat, idUsuario)) {
            throw new NegocioException(MENSAJE_NO_PARTICIPANTE);
        }
        return mensajeRepo.listarPorChat(idChat);
    }

    @Override
    public Mensaje obtenerMensaje(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("El mensaje no existe");
        }
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
