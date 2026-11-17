package co.edu.uniquindio.proyecto.convertidores;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CiudadServicio;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/** Convierte entre {@link Ciudad} y su id. Uso: {@code converter="#{ciudadConverter}"}. */
@Component
public class CiudadConverter implements Converter<Ciudad> {

    private static final String MENSAJE = "La ciudad seleccionada no es válida";

    private final CiudadServicio ciudadServicio;

    public CiudadConverter(CiudadServicio ciudadServicio) {
        this.ciudadServicio = ciudadServicio;
    }

    @Override
    public Ciudad getAsObject(FacesContext contexto, UIComponent componente, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return ciudadServicio.obtenerCiudad(Long.valueOf(valor.trim()));
        } catch (NumberFormatException | NegocioException e) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, MENSAJE, MENSAJE));
        }
    }

    @Override
    public String getAsString(FacesContext contexto, UIComponent componente, Ciudad ciudad) {
        return ciudad == null || ciudad.getId() == null ? "" : ciudad.getId().toString();
    }
}
