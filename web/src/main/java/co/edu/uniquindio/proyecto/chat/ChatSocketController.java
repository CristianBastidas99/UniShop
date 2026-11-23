package co.edu.uniquindio.proyecto.chat;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.MensajeServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Recibe los mensajes del chat por STOMP ({@code /app/chat/{idChat}}), los guarda y los publica
 * en {@code /topic/chat/{idChat}}. El remitente siempre sale del {@link Principal}, nunca del cliente.
 */
@Controller
public class ChatSocketController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatSocketController.class);
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Bogota"));

    private final MensajeServicio mensajeServicio;
    private final SimpMessagingTemplate plantilla;

    public ChatSocketController(MensajeServicio mensajeServicio, SimpMessagingTemplate plantilla) {
        this.mensajeServicio = mensajeServicio;
        this.plantilla = plantilla;
    }

    /**
     * Guarda el mensaje y lo publica a los suscritos del chat.
     *
     * @throws NegocioException si el servicio rechaza el mensaje (no participante, texto inválido...)
     */
    @MessageMapping("/chat/{idChat}")
    public void enviar(@DestinationVariable Long idChat, @Payload MensajeEntrada entrada, Principal principal)
            throws NegocioException {
        Long idRemitente = Long.valueOf(principal.getName());
        String texto = entrada == null ? null : entrada.getTexto();
        Mensaje guardado = mensajeServicio.enviarMensaje(idChat, idRemitente, texto);
        plantilla.convertAndSend("/topic/chat/" + idChat, aDto(guardado, idChat));
    }

    /** Los errores de negocio se devuelven solo a quien envió el mensaje. */
    @MessageExceptionHandler(NegocioException.class)
    @SendToUser(destinations = "/queue/errores", broadcast = false)
    public ErrorChatDTO manejarNegocio(NegocioException e) {
        return new ErrorChatDTO(e.getMessage());
    }

    /** Cualquier otro error se registra y se responde con un texto genérico. */
    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = "/queue/errores", broadcast = false)
    public ErrorChatDTO manejarInesperado(Exception e) {
        LOG.error("Error inesperado al enviar un mensaje de chat", e);
        return new ErrorChatDTO("No se pudo enviar el mensaje");
    }

    static MensajeChatDTO aDto(Mensaje m, Long idChat) {
        return new MensajeChatDTO(m.getId(), idChat, m.getMensaje(), m.getFecha().getTime(),
                FORMATO.format(m.getFecha().toInstant()),
                m.getRemitente().getId(), m.getRemitente().getNombre());
    }
}
