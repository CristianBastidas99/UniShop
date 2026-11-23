package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.servicios.ChatServicio;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Autoriza cada mensaje STOMP entrante: CONNECT exige un usuario autenticado; SUBSCRIBE solo permite
 * {@code /topic/chat/{id}} (si el usuario participa en el chat) y {@code /user/queue/errores};
 * SEND solo permite {@code /app/chat/{id}}. Todo lo demás se rechaza.
 */
@Component
public class AutorizacionChatInterceptor implements ChannelInterceptor {

    private static final Pattern DESTINO_CHAT = Pattern.compile("^/topic/chat/(\\d{1,18})$");
    private static final Pattern DESTINO_ENVIO = Pattern.compile("^/app/chat/(\\d{1,18})$");
    private static final String DESTINO_ERRORES = "/user/queue/errores";

    private final ChatServicio chatServicio;

    public AutorizacionChatInterceptor(ChatServicio chatServicio) {
        this.chatServicio = chatServicio;
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
            autorizarSuscripcion(principal, destino);
        } else if (StompCommand.SEND.equals(comando)) {
            if (principal == null || destino == null || !DESTINO_ENVIO.matcher(destino).matches()) {
                throw new MessagingException("Destino de envío no permitido");
            }
        }
        return message;
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
