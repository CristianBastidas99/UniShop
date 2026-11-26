package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.bean.SeguridadBean;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
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
 * con rol usuario (no visitantes ni administradores) y con la cuenta activa. Nunca crea una sesión nueva.
 * Guarda en los atributos de la conexión el id del usuario y la {@link HttpSession}, para que
 * {@link AutorizacionChatInterceptor} compruebe en cada mensaje que la sesión siga vigente.
 */
@Component
public class AutenticacionHandshakeInterceptor implements HandshakeInterceptor {

    /** Atributo de la sesión WebSocket donde se guarda el id (Long) del usuario. */
    public static final String ATRIBUTO_ID_USUARIO = "idUsuario";

    /** Atributo de la sesión WebSocket donde se guarda la {@link HttpSession} que autenticó la conexión. */
    public static final String ATRIBUTO_SESION_HTTP = "sesionHttp";

    private final UsuarioServicio usuarioServicio;

    public AutenticacionHandshakeInterceptor(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        HttpSession sesion = obtenerSesion(request);
        Long idUsuario = sesion == null ? null : obtenerIdUsuario(sesion);
        if (idUsuario == null || !usuarioServicio.estaActivo(idUsuario)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }
        attributes.put(ATRIBUTO_ID_USUARIO, idUsuario);
        attributes.put(ATRIBUTO_SESION_HTTP, sesion);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Nada que hacer después del handshake
    }

    private HttpSession obtenerSesion(ServerHttpRequest request) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return null;
        }
        HttpServletRequest servlet = ((ServletServerHttpRequest) request).getServletRequest();
        return servlet.getSession(false);
    }

    /** Id del usuario autenticado en esa sesión, o null si no hay un usuario (rol USUARIO) autenticado. */
    static Long obtenerIdUsuario(HttpSession sesion) {
        Object atributo;
        try {
            atributo = sesion.getAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD);
        } catch (IllegalStateException e) {
            return null; // la sesión fue invalidada
        }
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
