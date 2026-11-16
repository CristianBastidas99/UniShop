package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;

@Component
@ViewScoped
public class UsuarioBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(UsuarioBean.class);

    private final UsuarioServicio usuarioServicio;
    private Usuario usuario;

    public UsuarioBean(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @PostConstruct
    public void inicializar() {
        usuario = new Usuario();
    }

    public void registrarUsuario() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        try {
            usuarioServicio.registrarUsuario(usuario);
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Registro exitoso", "Tu cuenta fue creada, ya puedes iniciar sesión."));
            // Deja el formulario limpio
            usuario = new Usuario();
        } catch (NegocioException e) {
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo completar el registro", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error inesperado al registrar el usuario", e);
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo completar el registro", "Ocurrió un error inesperado. Inténtalo de nuevo más tarde."));
        }
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}
