package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.RecuperacionServicio;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;

/**
 * Bean de {@code recuperarPassword.xhtml}: pide el enlace de recuperación de contraseña. Siempre responde lo
 * mismo (esté o no registrado el correo) para no revelar qué correos existen; solo se avisa si el formato es inválido.
 */
@Component
@ViewScoped
public class RecuperarPasswordBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RecuperarPasswordBean.class);

    static final String MENSAJE_ENVIO = "Si el correo está registrado, te enviamos un enlace para restablecer la "
            + "contraseña. Revisa tu bandeja de entrada (vence en 30 minutos).";

    private final RecuperacionServicio recuperacionServicio;

    @Getter
    @Setter
    private String email;

    public RecuperarPasswordBean(RecuperacionServicio recuperacionServicio) {
        this.recuperacionServicio = recuperacionServicio;
    }

    public void enviarEnlace() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        try {
            recuperacionServicio.solicitarRecuperacion(email);
            email = null;
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, MENSAJE_ENVIO, null));
        } catch (NegocioException e) {
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        } catch (Exception e) {
            LOG.error("Error inesperado al solicitar la recuperación de contraseña", e);
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo procesar la solicitud. Inténtalo de nuevo más tarde.", null));
        }
    }
}
