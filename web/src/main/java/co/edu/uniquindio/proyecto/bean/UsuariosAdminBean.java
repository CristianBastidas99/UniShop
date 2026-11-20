package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean de {@code admin/usuarios.xhtml}: el administrador busca, lista, crea, edita, activa/desactiva y elimina
 * usuarios. Copia los datos a filas simples para no depender de la sesión de Hibernate.
 */
@Component
@ViewScoped
public class UsuariosAdminBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(UsuariosAdminBean.class);

    public static final int MAX_TELEFONOS = 3;

    /** Usuario ya copiado a datos simples. */
    public static class Fila implements Serializable {
        private final String nombre;
        private final String email;
        private final String ciudad;
        private final List<String> telefonos;
        private final boolean activo;

        Fila(String nombre, String email, String ciudad, List<String> telefonos, boolean activo) {
            this.nombre = nombre;
            this.email = email;
            this.ciudad = ciudad;
            this.telefonos = telefonos;
            this.activo = activo;
        }

        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
        public String getCiudad() { return ciudad; }
        public String getTelefonos() { return String.join(", ", telefonos); }
        public boolean isActivo() { return activo; }
        public String getEstado() { return activo ? "Activo" : "Desactivado"; }
    }

    /** Teléfono del formulario (un campo de texto). */
    public static class Telefono implements Serializable {
        private String numero;

        Telefono(String numero) { this.numero = numero; }

        public String getNumero() { return numero; }
        public void setNumero(String numero) { this.numero = numero; }
    }

    private final UsuarioServicio usuarioServicio;

    private List<Fila> usuarios = new ArrayList<>();
    private List<Fila> usuariosFiltrados;
    private String textoBusqueda;

    // Formulario de creación y edición
    private boolean edicion;
    private String nombre;
    private String email;
    private String password;
    private Ciudad ciudad;
    private List<Telefono> telefonos = new ArrayList<>();

    // Usuario elegido para eliminar
    private String emailSeleccionado;
    private String nombreSeleccionado;

    public UsuariosAdminBean(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        recargar();
    }

    /** Busca por nombre o correo (vacío = todos). */
    public void buscar() {
        recargar();
    }

    public void limpiarBusqueda() {
        textoBusqueda = null;
        recargar();
    }

    private void recargar() {
        usuarios = new ArrayList<>();
        usuariosFiltrados = null;
        try {
            for (Usuario u : usuarioServicio.buscarUsuarios(textoBusqueda)) {
                List<String> numeros = u.getNumeroTelefono() == null
                        ? new ArrayList<>() : new ArrayList<>(u.getNumeroTelefono());
                usuarios.add(new Fila(u.getNombre(), u.getEmail(),
                        u.getCiudad() == null ? "" : u.getCiudad().getNombre(), numeros, u.isActivo()));
            }
        } catch (Exception e) {
            errorInesperado("cargar los usuarios", e);
        }
    }

    // ---------- Crear y editar ----------

    public void prepararNuevo() {
        edicion = false;
        nombre = null;
        email = null;
        password = null;
        ciudad = null;
        telefonos = new ArrayList<>();
        telefonos.add(new Telefono(null));
    }

    public void prepararEdicion(Fila fila) {
        try {
            Usuario u = usuarioServicio.obtenerUsuario(fila.getEmail());
            edicion = true;
            nombre = u.getNombre();
            email = u.getEmail();
            password = null;
            ciudad = u.getCiudad();
            telefonos = new ArrayList<>();
            if (u.getNumeroTelefono() != null) {
                for (String numero : u.getNumeroTelefono()) {
                    telefonos.add(new Telefono(numero));
                }
            }
            if (telefonos.isEmpty()) {
                telefonos.add(new Telefono(null));
            }
        } catch (NegocioException e) {
            prepararNuevo();
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            prepararNuevo();
            errorInesperado("abrir el usuario", e);
        }
    }

    public void agregarTelefono() {
        if (telefonos.size() < MAX_TELEFONOS) {
            telefonos.add(new Telefono(null));
        }
    }

    /** Quita un teléfono; siempre debe quedar al menos uno. */
    public void quitarTelefono(Telefono telefono) {
        if (telefonos.size() > 1) {
            telefonos.remove(telefono);
        }
    }

    public boolean isPuedeAgregarTelefono() { return telefonos.size() < MAX_TELEFONOS; }
    public boolean isPuedeQuitarTelefono() { return telefonos.size() > 1; }

    /** Crea o actualiza el usuario. El diálogo solo se cierra (parámetro "ok") si todo salió bien. */
    public void guardar() {
        try {
            List<String> numeros = new ArrayList<>();
            for (Telefono t : telefonos) {
                if (t.getNumero() != null && !t.getNumero().trim().isEmpty()) {
                    numeros.add(t.getNumero().trim());
                }
            }
            Usuario u = new Usuario();
            u.setNombre(nombre == null ? null : nombre.trim());
            u.setEmail(email);
            u.setCiudad(ciudad);
            u.setNumeroTelefono(numeros);
            u.setPassword(password);
            if (edicion) {
                usuarioServicio.actualizarUsuario(u);
                aviso(FacesMessage.SEVERITY_INFO, "Usuario actualizado: " + u.getNombre());
            } else {
                usuarioServicio.registrarUsuario(u);
                aviso(FacesMessage.SEVERITY_INFO, "Usuario creado: " + u.getNombre());
            }
            PrimeFaces.current().ajax().addCallbackParam("ok", true);
            recargar();
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("guardar el usuario", e);
        }
    }

    // ---------- Activar, desactivar y eliminar ----------

    public void cambiarActivo(Fila fila) {
        try {
            boolean nuevo = !fila.isActivo();
            usuarioServicio.cambiarActivo(fila.getEmail(), nuevo);
            aviso(FacesMessage.SEVERITY_INFO,
                    "Usuario " + (nuevo ? "activado" : "desactivado") + ": " + fila.getNombre());
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("cambiar el estado del usuario", e);
        }
        recargar();
    }

    public void prepararEliminar(Fila fila) {
        emailSeleccionado = fila.getEmail();
        nombreSeleccionado = fila.getNombre();
    }

    /** Elimina al usuario; si tiene actividad el servicio lo rechaza y su mensaje sugiere desactivarlo. */
    public void eliminar() {
        try {
            usuarioServicio.eliminarUsuario(emailSeleccionado);
            aviso(FacesMessage.SEVERITY_INFO, "Usuario eliminado: " + nombreSeleccionado);
        } catch (NegocioException e) {
            aviso(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (Exception e) {
            errorInesperado("eliminar el usuario", e);
        }
        recargar();
    }

    private void errorInesperado(String accion, Exception e) {
        LOG.error("Error inesperado al {}", accion, e);
        aviso(FacesMessage.SEVERITY_ERROR, "No se pudo " + accion + ". Inténtalo de nuevo más tarde.");
    }

    private void aviso(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severidad, severidad == FacesMessage.SEVERITY_INFO ? "Listo" : "Atención", texto));
    }

    public List<Fila> getUsuarios() { return usuarios; }
    public List<Fila> getUsuariosFiltrados() { return usuariosFiltrados; }
    public void setUsuariosFiltrados(List<Fila> usuariosFiltrados) { this.usuariosFiltrados = usuariosFiltrados; }
    public String getTextoBusqueda() { return textoBusqueda; }
    public void setTextoBusqueda(String textoBusqueda) { this.textoBusqueda = textoBusqueda; }
    public boolean isEdicion() { return edicion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Ciudad getCiudad() { return ciudad; }
    public void setCiudad(Ciudad ciudad) { this.ciudad = ciudad; }
    public List<Telefono> getTelefonos() { return telefonos; }
    public String getNombreSeleccionado() { return nombreSeleccionado; }
}
