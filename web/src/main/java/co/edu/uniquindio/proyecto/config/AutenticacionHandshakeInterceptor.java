package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.bean.SeguridadBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Autentica el handshake del WebSocket con la sesión HTTP existente: solo pasa un usuario autenticado
 * con rol usuario (no visitantes ni administradores). Nunca crea una sesión nueva.
 */
@Component
public class AutenticacionHandshakeInterceptor implements HandshakeInterceptor {

    /** Atributo de la sesión WebSocket donde se guarda el id (Long) del usuario. */
    public static final String ATRIBUTO_ID_USUARIO = "idUsuario";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Long idUsuario = obtenerIdUsuario(request);
        if (idUsuario == null) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }
        attributes.put(ATRIBUTO_ID_USUARIO, idUsuario);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Nada que hacer después del handshake
    }

    private Long obtenerIdUsuario(ServerHttpRequest request) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return null;
        }
        HttpServletRequest servlet = ((ServletServerHttpRequest) request).getServletRequest();
        HttpSession sesion = servlet.getSession(false);
        if (sesion == null) {
            return null;
        }
        Object atributo = sesion.getAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD);
        if (!(atributo instanceof SeguridadBean)) {
            return null;
        }
        SeguridadBean seguridad = (SeguridadBean) atributo;
        if (!seguridad.isAutenticado() || !seguridad.isUsuario()) {
            return null;
        }
        return seguridad.getIdUsuario();
    }
}
