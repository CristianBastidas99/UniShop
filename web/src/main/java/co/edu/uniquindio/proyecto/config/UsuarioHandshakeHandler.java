package co.edu.uniquindio.proyecto.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Crea el {@link Principal} de la conexión a partir del id del usuario que dejó
 * {@link AutenticacionHandshakeInterceptor} en los atributos de la sesión WebSocket.
 */
public class UsuarioHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Object id = attributes.get(AutenticacionHandshakeInterceptor.ATRIBUTO_ID_USUARIO);
        return id instanceof Long ? new UsuarioPrincipal((Long) id) : null;
    }
}
