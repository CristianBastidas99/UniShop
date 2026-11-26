package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.bean.SeguridadBean;
import co.edu.uniquindio.proyecto.servicios.ChatServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import org.springframework.mock.web.MockHttpSession;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas unitarias de la autorización por mensaje STOMP: sin base de datos ni contexto de Spring. */
class AutorizacionChatInterceptorTest {

    private ChatServicio chatServicio;
    private UsuarioServicio usuarioServicio;
    private AutorizacionChatInterceptor interceptor;
    private final Principal usuario7 = new UsuarioPrincipal(7L);
    private MockHttpSession sesionHttp;
    private SeguridadBean seguridad;

    @BeforeEach
    void preparar() {
        chatServicio = mock(ChatServicio.class);
        usuarioServicio = mock(UsuarioServicio.class);
        interceptor = new AutorizacionChatInterceptor(chatServicio, usuarioServicio);
        // Sesión HTTP vigente de un usuario autenticado (id 7) con la cuenta activa
        seguridad = mock(SeguridadBean.class);
        when(seguridad.isAutenticado()).thenReturn(true);
        when(seguridad.isUsuario()).thenReturn(true);
        when(seguridad.getIdUsuario()).thenReturn(7L);
        when(usuarioServicio.estaActivo(7L)).thenReturn(true);
        sesionHttp = new MockHttpSession();
        sesionHttp.setAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD, seguridad);
    }

    private Message<byte[]> mensaje(StompCommand comando, String destino, Principal principal) {
        Map<String, Object> atributos = new HashMap<>();
        atributos.put(AutenticacionHandshakeInterceptor.ATRIBUTO_SESION_HTTP, sesionHttp);
        return mensaje(comando, destino, principal, atributos);
    }

    private Message<byte[]> mensaje(StompCommand comando, String destino, Principal principal,
                                    Map<String, Object> atributosSesion) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(comando);
        if (destino != null) {
            accessor.setDestination(destino);
        }
        accessor.setSessionId("s1");
        accessor.setUser(principal);
        accessor.setSessionAttributes(atributosSesion);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private void sesionTerminada(Message<?> m) {
        MessagingException e = assertThrows(MessagingException.class, () -> interceptor.preSend(m, null));
        assertEquals("Tu sesión terminó. Vuelve a iniciar sesión", e.getMessage());
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

    // ===== Sesión terminada o cuenta desactivada =====

    @Test
    void enviarConSesionInvalidadaSeRechazaConMensajeDeSesionTerminada() {
        when(chatServicio.esParticipante(45L, 7L)).thenReturn(true);
        Message<byte[]> envio = mensaje(StompCommand.SEND, "/app/chat/45", usuario7);
        permitido(envio);
        sesionHttp.invalidate(); // getAttribute lanza IllegalStateException
        sesionTerminada(envio);
    }

    @Test
    void suscribirseConSesionInvalidadaSeRechaza() {
        when(chatServicio.esParticipante(45L, 7L)).thenReturn(true);
        sesionHttp.invalidate();
        sesionTerminada(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/45", usuario7));
        sesionTerminada(mensaje(StompCommand.SUBSCRIBE, "/user/queue/errores", usuario7));
    }

    @Test
    void enviarSinSesionHttpEnLosAtributosSeRechaza() {
        sesionTerminada(mensaje(StompCommand.SEND, "/app/chat/45", usuario7, new HashMap<>()));
        sesionTerminada(mensaje(StompCommand.SEND, "/app/chat/45", usuario7, null));
    }

    @Test
    void enviarConSesionCerradaSesionDeSeguridadSeRechaza() {
        // La sesión sigue viva pero ya no tiene el bean de seguridad, o este ya no está autenticado
        when(seguridad.isAutenticado()).thenReturn(false);
        sesionTerminada(mensaje(StompCommand.SEND, "/app/chat/45", usuario7));
        when(seguridad.isAutenticado()).thenReturn(true);
        sesionHttp.removeAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD);
        sesionTerminada(mensaje(StompCommand.SEND, "/app/chat/45", usuario7));
    }

    @Test
    void enviarConSesionDeOtroUsuarioSeRechaza() {
        when(seguridad.getIdUsuario()).thenReturn(8L);
        when(usuarioServicio.estaActivo(8L)).thenReturn(true);
        sesionTerminada(mensaje(StompCommand.SEND, "/app/chat/45", usuario7));
    }

    @Test
    void enviarComoAdministradorSeRechaza() {
        when(seguridad.isUsuario()).thenReturn(false);
        sesionTerminada(mensaje(StompCommand.SEND, "/app/chat/45", usuario7));
    }

    @Test
    void enviarConUsuarioDesactivadoSeRechaza() {
        when(usuarioServicio.estaActivo(7L)).thenReturn(false);
        sesionTerminada(mensaje(StompCommand.SEND, "/app/chat/45", usuario7));
        when(chatServicio.esParticipante(45L, 7L)).thenReturn(true);
        sesionTerminada(mensaje(StompCommand.SUBSCRIBE, "/topic/chat/45", usuario7));
    }

    @Test
    void connectNoComprueba_laSesion() {
        sesionHttp.invalidate();
        permitido(mensaje(StompCommand.CONNECT, null, usuario7));
    }
}
