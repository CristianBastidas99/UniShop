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
import java.util.ArrayList;
import java.util.List;

@Component
@ViewScoped
public class UsuarioBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(UsuarioBean.class);

    private final UsuarioServicio usuarioServicio;
    private Usuario usuario;

    /** Máximo de teléfonos que se pueden registrar desde el formulario. */
    public static final int MAX_TELEFONOS = 3;

    /** Teléfonos del formulario: objetos mutables, porque un List<String> no se enlaza bien elemento a elemento. */
    private List<Telefono> telefonos;

    /** Teléfono del formulario (un campo de texto). */
    public static class Telefono implements Serializable {
        private String numero;

        public String getNumero() {
            return numero;
        }

        public void setNumero(String numero) {
            this.numero = numero;
        }
    }

    public UsuarioBean(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @PostConstruct
    public void inicializar() {
        usuario = new Usuario();
        telefonos = new ArrayList<>();
        telefonos.add(new Telefono());
    }

    public void agregarTelefono() {
        if (telefonos.size() < MAX_TELEFONOS) {
            telefonos.add(new Telefono());
        }
    }

    /** Quita un teléfono; siempre debe quedar al menos uno. */
    public void quitarTelefono(Telefono telefono) {
        if (telefonos.size() > 1) {
            telefonos.remove(telefono);
        }
    }

    public boolean isPuedeAgregarTelefono() {
        return telefonos.size() < MAX_TELEFONOS;
    }

    public boolean isPuedeQuitarTelefono() {
        return telefonos.size() > 1;
    }

    public String registrarUsuario() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        try {
            // Copia los teléfonos escritos (descarta los vacíos); el servicio valida el resto
            List<String> numeros = new ArrayList<>();
            for (Telefono t : telefonos) {
                if (t.getNumero() != null && !t.getNumero().trim().isEmpty()) {
                    numeros.add(t.getNumero().trim());
                }
            }
            usuario.setNumeroTelefono(numeros);
            usuarioServicio.registrarUsuario(usuario);
            // El mensaje sobrevive a la redirección y se ve en el login
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Registro exitoso, ya puedes iniciar sesión", null));
            contexto.getExternalContext().getFlash().setKeepMessages(true);
            return "/login?faces-redirect=true";
        } catch (NegocioException e) {
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo completar el registro", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error inesperado al registrar el usuario", e);
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo completar el registro", "Ocurrió un error inesperado. Inténtalo de nuevo más tarde."));
        }
        return null;
    }

    public List<Telefono> getTelefonos() {
        return telefonos;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}
