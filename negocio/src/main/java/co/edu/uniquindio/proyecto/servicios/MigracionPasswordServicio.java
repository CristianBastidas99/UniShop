package co.edu.uniquindio.proyecto.servicios;

/**
 * Migración de las contraseñas guardadas antes de encriptar (texto plano) al formato BCrypt.
 */
public interface MigracionPasswordServicio {

    /**
     * Encripta con BCrypt toda contraseña de usuarios y administradores que todavía no tenga formato BCrypt.
     * Es idempotente: las que ya están encriptadas no se tocan.
     *
     * @return cantidad de contraseñas que se encriptaron
     */
    int encriptarPasswordsPendientes();
}
