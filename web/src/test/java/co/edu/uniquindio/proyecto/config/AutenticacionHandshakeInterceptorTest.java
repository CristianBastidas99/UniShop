package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.bean.SeguridadBean;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas unitarias del handshake: sin base de datos ni contexto de Spring. */
class AutenticacionHandshakeInterceptorTest {

    private UsuarioServicio usuarioServicio;
    private AutenticacionHandshakeInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Map<String, Object> atributos;

    @BeforeEach
    void preparar() {
        usuarioServicio = mock(UsuarioServicio.class);
        when(usuarioServicio.estaActivo(7L)).thenReturn(true);
        interceptor = new AutenticacionHandshakeInterceptor(usuarioServicio);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        atributos = new HashMap<>();
    }

    private boolean handshake() {
        return interceptor.beforeHandshake(new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(response), null, atributos);
    }

    @Test
    void sinSesionSeRechazaYNoSeCreaSesion() {
        assertFalse(handshake());
        assertNull(request.getSession(false));
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(atributos.isEmpty());
    }

    @Test
    void sesionSinSeguridadSeRechaza() {
        request.getSession(true);
        assertFalse(handshake());
    }

    @Test
    void visitanteSeRechaza() {
        SeguridadBean seguridad = mock(SeguridadBean.class);
        when(seguridad.isAutenticado()).thenReturn(false);
        request.getSession(true).setAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD, seguridad);
        assertFalse(handshake());
    }

    @Test
    void administradorSeRechaza() {
        SeguridadBean seguridad = mock(SeguridadBean.class);
        when(seguridad.isAutenticado()).thenReturn(true);
        when(seguridad.isUsuario()).thenReturn(false);
        when(seguridad.isAdministrador()).thenReturn(true);
        request.getSession(true).setAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD, seguridad);
        assertFalse(handshake());
        assertTrue(atributos.isEmpty());
    }

    @Test
    void usuarioPasaYSuIdQuedaEnLosAtributos() {
        SeguridadBean seguridad = mock(SeguridadBean.class);
        when(seguridad.isAutenticado()).thenReturn(true);
        when(seguridad.isUsuario()).thenReturn(true);
        when(seguridad.getIdUsuario()).thenReturn(7L);
        request.getSession(true).setAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD, seguridad);

        assertTrue(handshake());
        assertEquals(7L, atributos.get(AutenticacionHandshakeInterceptor.ATRIBUTO_ID_USUARIO));
        // También guarda la sesión HTTP, para que AutorizacionChatInterceptor compruebe que siga vigente
        assertSame(request.getSession(false), atributos.get(AutenticacionHandshakeInterceptor.ATRIBUTO_SESION_HTTP));
    }

    @Test
    void usuarioDesactivadoSeRechaza() {
        SeguridadBean seguridad = mock(SeguridadBean.class);
        when(seguridad.isAutenticado()).thenReturn(true);
        when(seguridad.isUsuario()).thenReturn(true);
        when(seguridad.getIdUsuario()).thenReturn(7L);
        when(usuarioServicio.estaActivo(7L)).thenReturn(false);
        request.getSession(true).setAttribute(SeguridadFiltro.ATRIBUTO_SEGURIDAD, seguridad);

        assertFalse(handshake());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(atributos.isEmpty());
    }

    @Test
    void elHandshakeHandlerCreaElPrincipalConElId() {
        atributos.put(AutenticacionHandshakeInterceptor.ATRIBUTO_ID_USUARIO, 7L);
        var principal = new UsuarioHandshakeHandler()
                .determineUser(new ServletServerHttpRequest(request), null, atributos);
        assertEquals("7", principal.getName());
        assertNull(new UsuarioHandshakeHandler()
                .determineUser(new ServletServerHttpRequest(request), null, new HashMap<>()));
    }
}
