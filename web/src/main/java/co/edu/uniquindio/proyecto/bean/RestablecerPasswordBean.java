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
 * Bean de {@code restablecerPassword.xhtml?token=...}: valida el token del enlace y cambia la contraseña.
 * El token llega por GET, así que nunca se escribe en los registros (logs).
 */
@Component
@ViewScoped
public class RestablecerPasswordBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RestablecerPasswordBean.class);

    private final RecuperacionServicio recuperacionServicio;

    /** Valor del parámetro {@code token} del enlace. */
    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private String nueva;

    @Getter
    @Setter
    private String confirmacion;

    /** Indica si el enlace es válido (se calcula al abrir la página). */
    @Getter
    private boolean valido;

    public RestablecerPasswordBean(RecuperacionServicio recuperacionServicio) {
        this.recuperacionServicio = recuperacionServicio;
    }

    /** Se ejecuta con f:viewAction: marca si el enlace sirve; si no, la página muestra el aviso y no el formulario. */
    public void validar() {
        try {
            recuperacionServicio.validarToken(token);
            valido = true;
        } catch (NegocioException e) {
            valido = false;
        }
    }

    public String restablecer() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        try {
            recuperacionServicio.restablecerPassword(token, nueva, confirmacion);
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Tu contraseña se cambió. Ya puedes iniciar sesión.", null));
            contexto.getExternalContext().getFlash().setKeepMessages(true);
            return "/login?faces-redirect=true";
        } catch (NegocioException e) {
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            // Si el enlace dejó de servir, se oculta el formulario
            validar();
        } catch (Exception e) {
            LOG.error("Error inesperado al restablecer la contraseña", e);
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo cambiar la contraseña. Inténtalo de nuevo más tarde.", null));
        }
        nueva = null;
        confirmacion = null;
        return null;
    }
}
