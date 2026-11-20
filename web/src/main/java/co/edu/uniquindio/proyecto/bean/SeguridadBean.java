package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Persona;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.AdministradorServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Maneja la sesión de la persona que usa la aplicación: inicio y cierre de sesión
 * y consultas (autenticado, rol, nombre) para las vistas.
 */
@Component
@SessionScope
public class SeguridadBean implements Serializable {

    /** Rol de la persona autenticada. */
    public enum Rol { USUARIO, ADMINISTRADOR }

    private static final String MENSAJE_GENERICO = "Correo o contraseña incorrectos";

    private final UsuarioServicio usuarioServicio;
    private final AdministradorServicio administradorServicio;

    // Campos del formulario de login
    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String password;

    // Página privada pedida antes del login (se valida antes de usarla)
    @Getter
    @Setter
    private String destino;

    // Persona en sesión y su rol (null si no hay sesión). Sin getters: las vistas usan los métodos de consulta.
    private Persona persona;
    private Rol rol;

    public SeguridadBean(UsuarioServicio usuarioServicio, AdministradorServicio administradorServicio) {
        this.usuarioServicio = usuarioServicio;
        this.administradorServicio = administradorServicio;
    }

    public String iniciarSesion() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        ExternalContext externo = contexto.getExternalContext();

        // Evita la fijación de sesión: cambia el id conservando los atributos (y este bean) de la sesión
        ((HttpServletRequest) externo.getRequest()).changeSessionId();

        Persona encontrada;
        Rol rolEncontrado;
        try {
            encontrada = usuarioServicio.iniciarSesion(email, password);
            rolEncontrado = Rol.USUARIO;
        } catch (NegocioException e) {
            final String mensajeUsuario = e.getMessage();
            try {
                encontrada = administradorServicio.iniciarSesion(email, password);
                rolEncontrado = Rol.ADMINISTRADOR;
            } catch (NegocioException e2) {
                // El mensaje del intento de usuario solo es distinto del genérico cuando la contraseña era correcta
                // (p. ej. cuenta desactivada), así que no revela si un correo existe
                String mensaje = MENSAJE_GENERICO.equals(mensajeUsuario) ? e2.getMessage() : mensajeUsuario;
                contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensaje, null));
                password = null;
                return null;
            }
        }

        persona = encontrada;
        rol = rolEncontrado;
        password = null;
        email = null;

        contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Bienvenido(a), " + persona.getNombre(), null));
        externo.getFlash().setKeepMessages(true);
        // Vuelve a la página privada pedida si es válida para el rol; si no, al inicio
        String volver = destinoValido();
        destino = null;
        String destinoFinal = volver != null ? volver : (rol == Rol.ADMINISTRADOR ? "/admin/inicio" : "/index");
        return destinoFinal + (destinoFinal.contains("?") ? "&" : "?") + "faces-redirect=true";
    }

    /** Devuelve el destino solo si es una ruta interna permitida (privada para el rol o el detalle de un producto); si no, null. */
    private String destinoValido() {
        if (destino != null && destino.matches("/detalleProducto\\.xhtml\\?producto=[0-9]{1,18}")) {
            return destino;
        }
        if (destino != null && destino.matches("/subasta\\.xhtml\\?subasta=[0-9]{1,18}")) {
            return destino;
        }
        if (destino == null || !destino.matches("/(usuario|admin)/[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+)*\\.xhtml")) {
            return null;
        }
        if (destino.startsWith("/admin/") ? isAdministrador() : isUsuario()) {
            return destino;
        }
        return null;
    }

    public String cerrarSesion() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index?faces-redirect=true";
    }

    /** Si ya hay sesión, quien entra al login va al inicio (se usa desde f:viewAction). */
    public String redirigirSiAutenticado() {
        if (!isAutenticado()) {
            return null;
        }
        return isAdministrador() ? "/admin/inicio?faces-redirect=true" : "/index?faces-redirect=true";
    }

    public boolean isAutenticado() {
        return persona != null;
    }

    public boolean isUsuario() {
        return rol == Rol.USUARIO;
    }

    public boolean isAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public String getNombre() {
        return persona != null ? persona.getNombre() : null;
    }

    /** Id del usuario en sesión (solo si el rol es USUARIO); null en otro caso. No expone la persona. */
    public Long getIdUsuario() {
        return (persona != null && rol == Rol.USUARIO) ? persona.getId() : null;
    }

    /** Correo del administrador en sesión (solo si el rol es ADMINISTRADOR); null en otro caso. */
    public String getEmailAdministrador() {
        return (persona != null && rol == Rol.ADMINISTRADOR) ? persona.getEmail() : null;
    }
}
