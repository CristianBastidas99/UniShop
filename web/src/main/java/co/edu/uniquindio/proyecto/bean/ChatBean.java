package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.dto.chat.ChatResumenDTO;
import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ChatServicio;
import co.edu.uniquindio.proyecto.servicios.MensajeServicio;
import org.springframework.stereotype.Component;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Bean de {@code usuario/chat.xhtml?id=ID}: valida que el usuario en sesión participe en el chat y prepara
 * el encabezado y el historial. El envío y la recepción de mensajes nuevos los hace {@code chat.js} por STOMP,
 * no este bean. Si el chat no existe o el usuario no participa, no se carga ningún dato.
 */
@Component
@ViewScoped
public class ChatBean implements Serializable {

    /** Texto único para "no existe" y "no participas", para no revelar qué chats existen. */
    public static final String MENSAJE_SIN_ACCESO = "No tienes acceso a este chat o no existe";

    /** Mensaje del historial ya copiado a datos simples. */
    public static class MensajeVista implements Serializable {
        private final Long id;
        private final String texto;
        private final String fecha;
        private final String autor;
        private final boolean propio;

        MensajeVista(Long id, String texto, String fecha, String autor, boolean propio) {
            this.id = id;
            this.texto = texto;
            this.fecha = fecha;
            this.autor = autor;
            this.propio = propio;
        }

        public Long getId() { return id; }
        public String getTexto() { return texto; }
        public String getFecha() { return fecha; }
        public String getAutor() { return autor; }
        public boolean isPropio() { return propio; }
    }

    private static final String IMAGEN_POR_DEFECTO = "resources/img/sin-imagen.svg";

    private final ChatServicio chatServicio;
    private final MensajeServicio mensajeServicio;
    private final SeguridadBean seguridadBean;

    /** Valor crudo del parámetro {@code id} (texto, para tolerar valores no numéricos). */
    private String id;

    private Long idChat;
    private boolean acceso;
    private ChatResumenDTO resumen;
    private List<MensajeVista> mensajes = new ArrayList<>();

    public ChatBean(ChatServicio chatServicio, MensajeServicio mensajeServicio, SeguridadBean seguridadBean) {
        this.chatServicio = chatServicio;
        this.mensajeServicio = mensajeServicio;
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction: carga el chat solo si el usuario es participante. */
    public void cargar() {
        acceso = false;
        resumen = null;
        mensajes = new ArrayList<>();
        Long idUsuario = seguridadBean.getIdUsuario();
        try {
            idChat = Long.valueOf(id == null ? "" : id.trim());
        } catch (NumberFormatException e) {
            return;
        }
        if (!chatServicio.esParticipante(idChat, idUsuario)) {
            return;
        }
        try {
            for (ChatResumenDTO r : chatServicio.listarResumenChats(idUsuario)) {
                if (idChat.equals(r.getIdChat())) {
                    resumen = r;
                    break;
                }
            }
            if (resumen == null) {
                return;
            }
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            formato.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
            for (Mensaje m : mensajeServicio.listarMensajes(idChat, idUsuario)) {
                boolean propio = idUsuario.equals(m.getRemitente().getId());
                mensajes.add(new MensajeVista(m.getId(), m.getMensaje(), formato.format(m.getFecha()),
                        m.getRemitente().getNombre(), propio));
            }
            acceso = true;
        } catch (NegocioException e) {
            resumen = null;
            mensajes = new ArrayList<>();
        }
    }

    /** Ruta de la imagen del producto del chat, o la imagen por defecto si no tiene. */
    public String getImagen() {
        String contexto = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        String ruta = resumen == null || resumen.getImagenProducto() == null
                ? IMAGEN_POR_DEFECTO : resumen.getImagenProducto();
        return contexto + "/" + ruta;
    }

    /** Id del usuario en sesión, para que el JavaScript distinga los mensajes propios. */
    public Long getIdUsuario() {
        return seguridadBean.getIdUsuario();
    }

    public String getMensajeSinAcceso() { return MENSAJE_SIN_ACCESO; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getIdChat() { return idChat; }
    public boolean isAcceso() { return acceso; }
    public ChatResumenDTO getResumen() { return resumen; }
    public List<MensajeVista> getMensajes() { return mensajes; }
}
