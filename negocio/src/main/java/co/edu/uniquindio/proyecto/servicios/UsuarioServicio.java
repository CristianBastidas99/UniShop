package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Operaciones de negocio sobre usuarios. El correo es único entre usuarios y administradores.
 */
public interface UsuarioServicio {

    /**
     * Inicia sesión: busca por correo (sin espacios en los extremos y en minúsculas) y contraseña.
     * Si el correo no existe o la contraseña es errónea se lanza el mismo mensaje, para no revelar
     * si el correo está registrado.
     *
     * @throws NegocioException si el correo o la contraseña vienen nulos o vacíos, o si las credenciales
     *                          son incorrectas
     */
    Usuario iniciarSesion(String email, String password) throws NegocioException;

    /**
     * Registra un usuario nuevo. El correo se guarda sin espacios en los extremos y en minúsculas.
     * La ciudad es obligatoria y debe existir; se exige al menos un teléfono (solo dígitos, entre 7 y 15,
     * con un {@code +} opcional al inicio). Los teléfonos se recortan y los repetidos se descartan.
     *
     * @throws NegocioException si el id viene informado y ya existe, si el correo ya está registrado
     *                          (como usuario o como administrador), si la ciudad falta o no existe, o si
     *                          no hay teléfonos o alguno no es válido
     */
    Usuario registrarUsuario(Usuario u) throws NegocioException;

    /**
     * Actualiza un usuario buscándolo por su correo. Aplica las mismas validaciones de ciudad y
     * teléfonos que el registro.
     *
     * @throws NegocioException si no existe un usuario con ese correo, o si la ciudad o los teléfonos
     *                          no son válidos
     */
    Usuario actualizarUsuario(Usuario u) throws NegocioException;

    /**
     * Obtiene un usuario por su correo.
     *
     * @throws NegocioException si no existe un usuario con ese correo
     */
    Usuario obtenerUsuario(String email) throws NegocioException;

    /**
     * Elimina un usuario por su correo.
     *
     * @throws NegocioException si no existe un usuario con ese correo
     */
    void eliminarUsuario(String email) throws NegocioException;

    /** Lista todos los usuarios. */
    List<Usuario> listarUsuarios();
}
