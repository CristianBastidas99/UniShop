package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.dto.chat.ChatResumenDTO;
import co.edu.uniquindio.proyecto.dto.chat.RolChat;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ChatServicio;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean de {@code usuario/chats.xhtml} ("Mis chats"): lista los chats del usuario en sesión, como comprador
 * y como vendedor, con el último mensaje de cada uno y el más reciente primero.
 */
@Component
@ViewScoped
public class ChatsBean implements Serializable {

    /** Ruta (desde la raíz de la aplicación) de la imagen que se muestra si el producto no tiene imágenes. */
    private static final String IMAGEN_POR_DEFECTO = "resources/img/sin-imagen.svg";

    private final ChatServicio chatServicio;
    private final SeguridadBean seguridadBean;

    private List<ChatResumenDTO> chats = new ArrayList<>();

    public ChatsBean(ChatServicio chatServicio, SeguridadBean seguridadBean) {
        this.chatServicio = chatServicio;
        this.seguridadBean = seguridadBean;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        try {
            chats = chatServicio.listarResumenChats(seguridadBean.getIdUsuario());
        } catch (NegocioException e) {
            chats = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", e.getMessage()));
        }
    }

    /** Ruta de la imagen del producto del chat, o la imagen por defecto si no tiene. */
    public String imagen(ChatResumenDTO chat) {
        String contexto = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        String ruta = chat.getImagenProducto() == null ? IMAGEN_POR_DEFECTO : chat.getImagenProducto();
        return contexto + "/" + ruta;
    }

    /** Etiqueta del papel del usuario en el chat: "Comprando" o "Vendiendo". */
    public String etiquetaRol(ChatResumenDTO chat) {
        return chat.getRol() == RolChat.VENDEDOR ? "Vendiendo" : "Comprando";
    }

    /** Clase CSS de la etiqueta del papel. */
    public String claseRol(ChatResumenDTO chat) {
        return chat.getRol() == RolChat.VENDEDOR ? "chat-rol chat-rol-vendiendo" : "chat-rol chat-rol-comprando";
    }

    public List<ChatResumenDTO> getChats() {
        return chats;
    }
}
