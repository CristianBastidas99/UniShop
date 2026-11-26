package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.bean.SeguridadBean;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controla el acceso a las páginas. Se aplica a todas las páginas {@code *.xhtml}:
 * <ul>
 *   <li>Rutas privadas: /usuario/* (solo usuarios) y /admin/* (solo administradores). Sin sesión redirige al
 *   login (recordando la página pedida); con el rol equivocado, al inicio.</li>
 *   <li>Cualquier página: si hay un usuario en sesión, se comprueba en la base que su cuenta siga activa; si el
 *   administrador la desactivó (o ya no existe) se cierra su sesión y se le envía a
 *   {@code /login.xhtml?motivo=desactivado}.</li>
 * </ul>
 */
public class SeguridadFiltro implements Filter {

    /** Nombre del atributo de sesión donde Spring guarda el SeguridadBean (@SessionScope). */
    static final String ATRIBUTO_SEGURIDAD = "scopedTarget.seguridadBean";

    /** Valor del parámetro {@code motivo} del login cuando se cerró la sesión por cuenta desactivada. */
    public static final String MOTIVO_DESACTIVADO = "desactivado";

    private static final String RUTA_LOGIN = "/login.xhtml";

    /** Prefijo de los recursos que sirve JSF (también terminan en .xhtml). */
    private static final String RUTA_RECURSOS = "/javax.faces.resource/";

    private final UsuarioServicio usuarioServicio;

    public SeguridadFiltro(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (ruta.startsWith(RUTA_RECURSOS)) {
            // Los recursos de JSF (CSS, JS, imágenes) no son páginas: no se consulta la base por cada uno
            chain.doFilter(req, res);
            return;
        }
        boolean privada = ruta.startsWith("/usuario/") || ruta.startsWith("/admin/");

        if (privada) {
            // Las páginas protegidas no se cachean: tras cerrar sesión, "atrás" no muestra contenido privado
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }

        // No se crea sesión si no existe
        HttpSession sesion = request.getSession(false);
        SeguridadBean seguridad = null;
        if (sesion != null) {
            try {
                Object atributo = sesion.getAttribute(ATRIBUTO_SEGURIDAD);
                if (atributo instanceof SeguridadBean) {
                    seguridad = (SeguridadBean) atributo;
                }
            } catch (IllegalStateException e) {
                seguridad = null; // la sesión ya fue invalidada por otra petición
            }
        }
        boolean autenticado = seguridad != null && seguridad.isAutenticado();

        // Un usuario desactivado pierde la sesión en su siguiente petición de página (en cualquier página)
        if (autenticado && seguridad.isUsuario() && !usuarioServicio.estaActivo(seguridad.getIdUsuario())) {
            try {
                sesion.invalidate();
            } catch (IllegalStateException e) {
                // ya estaba invalidada
            }
            if (RUTA_LOGIN.equals(ruta)) {
                // Evita un bucle: el login (ya sin sesión) se muestra con su mensaje
                chain.doFilter(req, res);
                return;
            }
            redirigir(request, response, RUTA_LOGIN + "?motivo=" + MOTIVO_DESACTIVADO);
            return;
        }

        if (!privada) {
            chain.doFilter(req, res);
            return;
        }

        if (!autenticado) {
            redirigir(request, response, RUTA_LOGIN + "?destino=" + URLEncoder.encode(ruta, StandardCharsets.UTF_8.name()));
            return;
        }

        boolean permitido = ruta.startsWith("/admin/") ? seguridad.isAdministrador() : seguridad.isUsuario();
        if (!permitido) {
            redirigir(request, response, "/index.xhtml");
            return;
        }
        chain.doFilter(req, res);
    }

    /** Redirige; en peticiones ajax de JSF responde con el XML de redirección parcial. */
    private void redirigir(HttpServletRequest request, HttpServletResponse response, String destino) throws IOException {
        String url = response.encodeRedirectURL(request.getContextPath() + destino);
        if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            String xml = url.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;");
            response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<partial-response><redirect url=\"" + xml + "\"/></partial-response>");
        } else {
            response.sendRedirect(url);
        }
    }
}
