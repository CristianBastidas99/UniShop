package co.edu.uniquindio.proyecto.rest;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cuerpo de los errores que no pasan por ningún controlador y terminan en {@code /error} de Spring Boot
 * (por ejemplo, una ruta {@code /api/...} que no existe). Para las rutas de la API devuelve
 * {@code { "mensaje": "..." }}, igual que {@link ManejadorErroresRest}.
 * <p>
 * Para el resto de rutas (las páginas JSF también terminan en {@code /error}) no cambia nada: usa los
 * atributos por defecto de Spring Boot. El código HTTP lo sigue decidiendo {@code BasicErrorController}
 * a partir de la petición, no de este mapa.
 * <p>
 * Dentro de {@code /error}, {@code getRequestURI()} devuelve {@code /error}; la ruta original se lee del
 * atributo {@link RequestDispatcher#ERROR_REQUEST_URI}.
 */
@Component
public class ErroresApiAtributos extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> atributos = super.getErrorAttributes(webRequest, options);
        String ruta = rutaOriginal(webRequest);
        if (!esRutaApi(ruta, webRequest.getContextPath())) {
            return atributos;
        }
        Object codigo = atributos.get("status");
        HttpStatus estado = codigo instanceof Integer ? HttpStatus.resolve((Integer) codigo) : null;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", mensajePara(estado, ruta));
        return cuerpo;
    }

    private static String rutaOriginal(WebRequest webRequest) {
        Object ruta = webRequest.getAttribute(RequestDispatcher.ERROR_REQUEST_URI, RequestAttributes.SCOPE_REQUEST);
        return ruta instanceof String ? (String) ruta : null;
    }

    /** {@code true} si la ruta (sin el context path) es {@code /api} o empieza por {@code /api/}. */
    static boolean esRutaApi(String ruta, String contextPath) {
        if (ruta == null) {
            return false;
        }
        String relativa = (contextPath != null && !contextPath.isEmpty() && ruta.startsWith(contextPath))
                ? ruta.substring(contextPath.length()) : ruta;
        return relativa.equals("/api") || relativa.startsWith("/api/");
    }

    private static String mensajePara(HttpStatus estado, String ruta) {
        if (estado == HttpStatus.NOT_FOUND) {
            return "La ruta " + ruta + " no existe";
        }
        if (estado == HttpStatus.METHOD_NOT_ALLOWED) {
            return "Método no permitido en esta ruta";
        }
        return estado == null ? ManejadorErroresRest.MENSAJE_INESPERADO : ManejadorErroresRest.textoGenerico(estado);
    }
}
