package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.chat.ChatResumenDTO;
import co.edu.uniquindio.proyecto.dto.chat.RolChat;
import co.edu.uniquindio.proyecto.entidades.Chat;
import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.MensajeRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatServicioImpl implements ChatServicio {

    private final ChatRepo chatRepo;
    private final UsuarioRepo usuarioRepo;
    private final ProductoRepo productoRepo;
    private final MensajeRepo mensajeRepo;

    /** Largo máximo del último mensaje en el resumen (con los puntos suspensivos incluidos). */
    static final int LARGO_RESUMEN = 80;

    public ChatServicioImpl(ChatRepo chatRepo, UsuarioRepo usuarioRepo, ProductoRepo productoRepo,
                            MensajeRepo mensajeRepo) {
        this.mensajeRepo = mensajeRepo;
        this.chatRepo = chatRepo;
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
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
    @Transactional(rollbackFor = NegocioException.class)
    public Chat obtenerOCrearChat(Long idComprador, Long idProducto) throws NegocioException {
        Usuario comprador = idComprador == null ? null : usuarioRepo.findById(idComprador).orElse(null);
        if (comprador == null) {
            throw new NegocioException("El usuario no existe");
        }
        Producto producto = idProducto == null ? null : productoRepo.findById(idProducto).orElse(null);
        if (producto == null) {
            throw new NegocioException("El producto no existe");
        }
        if (producto.getUsuario() != null && idComprador.equals(producto.getUsuario().getId())) {
            throw new NegocioException("No puede abrir un chat sobre su propio producto");
        }
        Optional<Chat> existente = chatRepo.buscarPorCompradorYProducto(idComprador, idProducto);
        if (existente.isPresent()) {
            return existente.get();
        }
        if (producto.getFechaLimite() == null || !producto.getFechaLimite().after(new Date())) {
            throw new NegocioException("El producto no está disponible");
        }
        Chat chat = new Chat();
        chat.setUsuario(comprador);
        chat.setProducto(producto);
        return chatRepo.save(chat);
    }

    @Override
    public List<Chat> listarChatsUsuario(Long idUsuario) throws NegocioException {
        if (idUsuario == null || !usuarioRepo.existsById(idUsuario)) {
            throw new NegocioException("El usuario no existe");
        }
        return chatRepo.listarPorUsuario(idUsuario);
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = NegocioException.class)
    public List<ChatResumenDTO> listarResumenChats(Long idUsuario) throws NegocioException {
        if (idUsuario == null || !usuarioRepo.existsById(idUsuario)) {
            throw new NegocioException("El usuario no existe");
        }
        List<Chat> chats = chatRepo.listarPorUsuario(idUsuario);
        if (chats.isEmpty()) {
            return new ArrayList<>();
        }
        // Una sola consulta para los últimos mensajes de todos los chats
        List<Long> ids = new ArrayList<>();
        chats.forEach(c -> ids.add(c.getId()));
        Map<Long, Mensaje> ultimos = new HashMap<>();
        for (Mensaje m : mensajeRepo.listarUltimosPorChats(ids)) {
            Mensaje actual = ultimos.get(m.getChat().getId());
            if (actual == null || m.getId() > actual.getId()) {
                ultimos.put(m.getChat().getId(), m);
            }
        }
        List<ChatResumenDTO> resumen = new ArrayList<>();
        for (Chat c : chats) {
            resumen.add(resumir(c, idUsuario, ultimos.get(c.getId())));
        }
        // Más reciente primero; sin mensajes al final (y entre ellos, el chat más nuevo primero)
        resumen.sort(Comparator
                .comparing(ChatResumenDTO::getFechaUltimoMensaje, Comparator.nullsLast(Comparator.<Date>reverseOrder()))
                .thenComparing(ChatResumenDTO::getIdChat, Comparator.reverseOrder()));
        return resumen;
    }

    private ChatResumenDTO resumir(Chat chat, Long idUsuario, Mensaje ultimo) {
        boolean comprador = idUsuario.equals(chat.getUsuario().getId());
        Usuario otra = comprador ? chat.getProducto().getUsuario() : chat.getUsuario();
        ChatResumenDTO dto = new ChatResumenDTO();
        dto.setIdChat(chat.getId());
        dto.setRol(comprador ? RolChat.COMPRADOR : RolChat.VENDEDOR);
        dto.setIdOtraPersona(otra.getId());
        dto.setNombreOtraPersona(otra.getNombre());
        dto.setIdProducto(chat.getProducto().getId());
        dto.setNombreProducto(chat.getProducto().getNombre());
        List<String> imagenes = chat.getProducto().getMisImagenes();
        dto.setImagenProducto(imagenes == null || imagenes.isEmpty() ? null : imagenes.get(0));
        if (ultimo != null) {
            dto.setUltimoMensaje(recortar(ultimo.getMensaje()));
            dto.setFechaUltimoMensaje(ultimo.getFecha());
            dto.setUltimoMensajePropio(ultimo.getRemitente() != null
                    && idUsuario.equals(ultimo.getRemitente().getId()));
        }
        return dto;
    }

    private static String recortar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.length() <= LARGO_RESUMEN ? limpio : limpio.substring(0, LARGO_RESUMEN - 1).trim() + "…";
    }

    @Override
    public boolean esParticipante(Long idChat, Long idUsuario) {
        if (idChat == null || idUsuario == null) {
            return false;
        }
        return chatRepo.findById(idChat)
                .map(c -> esParticipante(c, idUsuario))
                .orElse(false);
    }

    /** Indica si el usuario es el comprador del chat o el vendedor de su producto. */
    static boolean esParticipante(Chat chat, Long idUsuario) {
        if (idUsuario == null) {
            return false;
        }
        boolean comprador = chat.getUsuario() != null && idUsuario.equals(chat.getUsuario().getId());
        boolean vendedor = chat.getProducto() != null && chat.getProducto().getUsuario() != null
                && idUsuario.equals(chat.getProducto().getUsuario().getId());
        return comprador || vendedor;
    }

    @Override
    public Chat obtenerChat(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("El chat no existe");
        }
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
