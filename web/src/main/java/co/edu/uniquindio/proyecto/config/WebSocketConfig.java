package co.edu.uniquindio.proyecto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Configuración del chat en tiempo real con STOMP sobre WebSocket (con respaldo SockJS).
 * <p>
 * El endpoint es {@code /ws} y solo acepta el mismo origen. La conexión exige un usuario autenticado
 * ({@link AutenticacionHandshakeInterceptor}) y cada suscripción o envío se autoriza en
 * {@link AutorizacionChatInterceptor}.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** Tamaño máximo de un mensaje STOMP en bytes (el texto del chat tiene como máximo 1000 caracteres). */
    static final int TAMANO_MAXIMO_MENSAJE = 16 * 1024;

    private final AutenticacionHandshakeInterceptor handshakeInterceptor;
    private final AutorizacionChatInterceptor autorizacionInterceptor;
    private final String contexto;

    public WebSocketConfig(AutenticacionHandshakeInterceptor handshakeInterceptor,
                           AutorizacionChatInterceptor autorizacionInterceptor,
                           @Value("${server.servlet.context-path:}") String contexto) {
        this.handshakeInterceptor = handshakeInterceptor;
        this.autorizacionInterceptor = autorizacionInterceptor;
        this.contexto = contexto == null || "/".equals(contexto) ? "" : contexto;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Sin setAllowedOrigins: solo se acepta el mismo origen
        registry.addEndpoint("/ws")
                .addInterceptors(handshakeInterceptor)
                .setHandshakeHandler(new UsuarioHandshakeHandler())
                .withSockJS()
                // La librería de SockJS se sirve desde el WebJar local, no desde un CDN
                .setClientLibraryUrl(contexto + "/webjars/sockjs-client/1.5.1/sockjs.min.js");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(autorizacionInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(TAMANO_MAXIMO_MENSAJE);
        registration.setSendBufferSizeLimit(512 * 1024);
        registration.setSendTimeLimit(20 * 1000);
    }
}
