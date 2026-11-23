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
     * @throws NegocioException si el correo o la contraseña vienen nulos o vacíos, si las credenciales
     *                          son incorrectas o si la cuenta está desactivada
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
     * Actualiza un usuario buscándolo por su correo. Solo copia nombre, ciudad, teléfonos y contraseña
     * (esta última solo si viene no vacía; si es nula o en blanco se conserva la actual); el correo, el estado
     * activo y las relaciones no cambian. Aplica las mismas validaciones de ciudad y
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
     * Elimina un usuario por su correo. Si tiene actividad (ver {@link #tieneActividad(String)}) no se puede
     * eliminar y se debe desactivar. Sus teléfonos y favoritos se borran con él.
     *
     * @throws NegocioException si no existe un usuario con ese correo o si tiene actividad
     */
    void eliminarUsuario(String email) throws NegocioException;

    /** Lista todos los usuarios. */
    List<Usuario> listarUsuarios();

    /**
     * Activa o desactiva la cuenta de un usuario. Un usuario desactivado no puede iniciar sesión.
     *
     * @throws NegocioException si no existe un usuario con ese correo
     */
    void cambiarActivo(String email, boolean activo) throws NegocioException;

    /**
     * Indica si el usuario tiene actividad en la tienda: productos publicados, compras, comentarios,
     * chats (como comprador), mensajes enviados, pujas o reportes.
     *
     * @throws NegocioException si no existe un usuario con ese correo
     */
    boolean tieneActividad(String email) throws NegocioException;

    /**
     * Busca usuarios cuyo nombre o correo contenga el texto (parcial, sin distinguir mayúsculas).
     * Un texto nulo o vacío devuelve todos los usuarios. El resultado va ordenado por nombre.
     */
    List<Usuario> buscarUsuarios(String texto);

    /**
     * Cambia la contraseña del usuario con ese correo. Es el ÚNICO lugar donde se cambia la contraseña de un
     * usuario por la recuperación por correo. Hoy se guarda en texto plano; en el Paso 10.1 aquí se
     * encriptará. No exige que la cuenta esté activa.
     *
     * @throws NegocioException si no existe un usuario con ese correo o la contraseña viene vacía
     */
    void cambiarPassword(String email, String nueva) throws NegocioException;
}
