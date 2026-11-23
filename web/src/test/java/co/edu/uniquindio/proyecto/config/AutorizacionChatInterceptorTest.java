package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.servicios.ChatServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas unitarias de la autorización por mensaje STOMP: sin base de datos ni contexto de Spring. */
class AutorizacionChatInterceptorTest {

    private ChatServicio chatServicio;
    private AutorizacionChatInterceptor interceptor;
    private final Principal usuario7 = new UsuarioPrincipal(7L);

    @BeforeEach
    void preparar() {
        chatServicio = mock(ChatServicio.class);
        interceptor = new AutorizacionChatInterceptor(chatServicio);
    }

    private Message<byte[]> mensaje(StompCommand comando, String destino, Principal principal) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(comando);
        if (destino != null) {
            accessor.setDestination(destino);
        }
        accessor.setSessionId("s1");
        accessor.setUser(principal);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private void permitido(Message<?> m) {
        assertSame(m, interceptor.preSend(m, null));
    }

    private void rechazado(Message<?> m) {
        assertThrows(MessagingException.class, () -> interceptor.preSend(m, null));
    }

    @Test
    void connectSinPrincipalSeRechaza() {
        rechazado(mensaje(StompCommand.CONNECT, null, null));
    }

    @Test
    void connectConPrincipalPasa() {
        permitido(mensaje(StompCommand.CONNECT, null, usuario7));
    }

    @Test
    void suscribirseAChatDondeEsParticipantePasa() {
        when(chatServicio.esParticipante(45L, 7L)).thenReturn(true);
        permitido(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/45", usuario7));
    }

    @Test
    void suscribirseAChatAjenoSeRechaza() {
        when(chatServicio.esParticipante(45L, 7L)).thenReturn(false);
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/45", usuario7));
    }

    @Test
    void suscribirseSinPrincipalSeRechaza() {
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/45", null));
    }

    @Test
    void suscribirseALaColaDeErroresPasa() {
        permitido(mensaje(StompCommand.SUBSCRIBE, "/user/queue/errores", usuario7));
    }

    @Test
    void suscribirseADestinosNoPermitidosSeRechaza() {
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/topic/otro", usuario7));
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/45/extra", usuario7));
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/", usuario7));
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/queue/errores", usuario7));
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/user/queue/otra", usuario7));
        rechazado(mensaje(StompCommand.SUBSCRIBE, null, usuario7));
    }

    @Test
    void suscribirseConIdNoNumericoSeRechaza() {
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/abc", usuario7));
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/-1", usuario7));
        rechazado(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/99999999999999999999999", usuario7));
    }

    @Test
    void enviarAChatPasa() {
        permitido(mensaje(StompCommand.SEND, "/app/chat/45", usuario7));
    }

    @Test
    void enviarAOtroDestinoSeRechaza() {
        rechazado(mensaje(StompCommand.SEND, "/topic/chat/45", usuario7));
        rechazado(mensaje(StompCommand.SEND, "/app/otro", usuario7));
        rechazado(mensaje(StompCommand.SEND, "/app/chat/abc", usuario7));
        rechazado(mensaje(StompCommand.SEND, "/app/chat/45", null));
    }
}
