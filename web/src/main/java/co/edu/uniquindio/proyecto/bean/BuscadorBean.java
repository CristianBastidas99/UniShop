package co.edu.uniquindio.proyecto.bean;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Buscador del encabezado (está en todas las páginas, por eso es de petición y no hace consultas):
 * solo redirige a la página de resultados con el texto en el parámetro {@code q}.
 */
@Component
@RequestScope
public class BuscadorBean {

    @Getter
    @Setter
    private String texto;

    /** En la página de resultados el campo muestra el texto buscado (parámetro q de la URL). */
    @PostConstruct
    public void inicializar() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        if (contexto != null) {
            texto = contexto.getExternalContext().getRequestParameterMap().get("q");
        }
    }

    /** Redirige a los resultados; el texto se codifica para que espacios, tildes o "&" no rompan la URL. */
    public String buscar() {
        return "/resultadoBusqueda?faces-redirect=true&q=" + codificar(texto == null ? "" : texto.trim());
    }

    /** Codifica un valor para usarlo en una URL (UTF-8). */
    static String codificar(String valor) {
        try {
            return URLEncoder.encode(valor, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
