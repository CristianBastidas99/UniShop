package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.bean.SeguridadBean;

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
 * Controla el acceso a las rutas privadas: /usuario/* (solo usuarios) y /admin/* (solo administradores).
 * Sin sesión redirige al login (recordando la página pedida); con el rol equivocado, al inicio.
 */
public class SeguridadFiltro implements Filter {

    /** Nombre del atributo de sesión donde Spring guarda el SeguridadBean (@SessionScope). */
    static final String ATRIBUTO_SEGURIDAD = "scopedTarget.seguridadBean";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Las páginas protegidas no se cachean: tras cerrar sesión, "atrás" no muestra contenido privado
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String ruta = request.getRequestURI().substring(request.getContextPath().length());

        // No se crea sesión si no existe
        HttpSession sesion = request.getSession(false);
        SeguridadBean seguridad = null;
        if (sesion != null) {
            Object atributo = sesion.getAttribute(ATRIBUTO_SEGURIDAD);
            if (atributo instanceof SeguridadBean) {
                seguridad = (SeguridadBean) atributo;
            }
        }

        if (seguridad == null || !seguridad.isAutenticado()) {
            redirigir(request, response, "/login.xhtml?destino=" + URLEncoder.encode(ruta, StandardCharsets.UTF_8.name()));
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
