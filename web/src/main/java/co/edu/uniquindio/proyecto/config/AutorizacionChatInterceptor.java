package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.bean.SeguridadBean;
import co.edu.uniquindio.proyecto.servicios.ChatServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Autoriza cada mensaje STOMP entrante: CONNECT exige un usuario autenticado; SUBSCRIBE solo permite
 * {@code /topic/chat/{id}} (si el usuario participa en el chat) y {@code /user/queue/errores};
 * SEND solo permite {@code /app/chat/{id}}. Todo lo demás se rechaza.
 * <p>
 * Además, en SUBSCRIBE y SEND comprueba que la sesión HTTP que autenticó la conexión siga vigente, que su
 * usuario siga autenticado y que su cuenta siga activa. Si no, lanza {@link #MENSAJE_SESION_TERMINADA}: Spring
 * responde con un frame ERROR y cierra la conexión WebSocket, y el JS del chat redirige al login.
 */
@Component
public class AutorizacionChatInterceptor implements ChannelInterceptor {

    private static final Pattern DESTINO_CHAT = Pattern.compile("^/topic/chat/(\\d{1,18})$");
    private static final Pattern DESTINO_ENVIO = Pattern.compile("^/app/chat/(\\d{1,18})$");
    private static final String DESTINO_ERRORES = "/user/queue/errores";

    /** Mensaje del error cuando la sesión terminó o la cuenta fue desactivada (el JS del chat lo reconoce). */
    public static final String MENSAJE_SESION_TERMINADA = "Tu sesión terminó. Vuelve a iniciar sesión";

    private final ChatServicio chatServicio;
    private final UsuarioServicio usuarioServicio;

    public AutorizacionChatInterceptor(ChatServicio chatServicio, UsuarioServicio usuarioServicio) {
        this.chatServicio = chatServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        StompCommand comando = accessor.getCommand();
        Principal principal = accessor.getUser();
        String destino = accessor.getDestination();

        if (StompCommand.CONNECT.equals(comando) || StompCommand.STOMP.equals(comando)) {
            if (principal == null) {
                throw new MessagingException("Conexión no autenticada");
            }
        } else if (StompCommand.SUBSCRIBE.equals(comando)) {
            validarSesionVigente(principal, accessor.getSessionAttributes());
            autorizarSuscripcion(principal, destino);
        } else if (StompCommand.SEND.equals(comando)) {
            if (principal == null || destino == null || !DESTINO_ENVIO.matcher(destino).matches()) {
                throw new MessagingException("Destino de envío no permitido");
            }
            validarSesionVigente(principal, accessor.getSessionAttributes());
        }
        return message;
    }

    /**
     * Verifica que la sesión HTTP guardada en el handshake siga válida, que su {@link SeguridadBean} siga autenticado
     * con el mismo usuario del principal y que el usuario siga activo en la base de datos.
     */
    private void validarSesionVigente(Principal principal, Map<String, Object> atributos) {
        if (principal == null || atributos == null) {
            throw new MessagingException(MENSAJE_SESION_TERMINADA);
        }
        Object sesion = atributos.get(AutenticacionHandshakeInterceptor.ATRIBUTO_SESION_HTTP);
        if (!(sesion instanceof HttpSession)) {
            throw new MessagingException(MENSAJE_SESION_TERMINADA);
        }
        Object atributo;
        try {
            // Si la sesión fue invalidada (cierre de sesión o expiración), getAttribute lanza IllegalStateException
            atributo = ((HttpSession) sesion).getAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD);
        } catch (IllegalStateException e) {
            throw new MessagingException(MENSAJE_SESION_TERMINADA);
        }
        if (!(atributo instanceof SeguridadBean)) {
            throw new MessagingException(MENSAJE_SESION_TERMINADA);
        }
        SeguridadBean seguridad = (SeguridadBean) atributo;
        Long idSesion = seguridad.isAutenticado() && seguridad.isUsuario() ? seguridad.getIdUsuario() : null;
        if (idSesion == null || !idSesion.toString().equals(principal.getName())
                || !usuarioServicio.estaActivo(idSesion)) {
            throw new MessagingException(MENSAJE_SESION_TERMINADA);
        }
    }

    private void autorizarSuscripcion(Principal principal, String destino) {
        if (principal == null || destino == null) {
            throw new MessagingException("Suscripción no permitida");
        }
        if (DESTINO_ERRORES.equals(destino)) {
            return;
        }
        Matcher m = DESTINO_CHAT.matcher(destino);
        if (!m.matches()) {
            throw new MessagingException("Destino de suscripción no permitido");
        }
        Long idChat;
        Long idUsuario;
        try {
            idChat = Long.valueOf(m.group(1));
            idUsuario = Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            throw new MessagingException("Suscripción no permitida");
        }
        if (!chatServicio.esParticipante(idChat, idUsuario)) {
            throw new MessagingException("No participa en este chat");
        }
    }
}
