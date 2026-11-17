package co.edu.uniquindio.proyecto.convertidores;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CategoriaServicio;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/** Convierte entre {@link Categoria} y su id. Uso: {@code converter="#{categoriaConverter}"}. */
@Component
public class CategoriaConverter implements Converter<Categoria> {

    private static final String MENSAJE = "La categoría seleccionada no es válida";

    private final CategoriaServicio categoriaServicio;

    public CategoriaConverter(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    @Override
    public Categoria getAsObject(FacesContext contexto, UIComponent componente, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return categoriaServicio.obtenerCategoria(Long.valueOf(valor.trim()));
        } catch (NumberFormatException | NegocioException e) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, MENSAJE, MENSAJE));
        }
    }

    @Override
    public String getAsString(FacesContext contexto, UIComponent componente, Categoria categoria) {
        return categoria == null || categoria.getId() == null ? "" : categoria.getId().toString();
    }
}
