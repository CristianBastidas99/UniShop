package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.excepciones.NegocioException;

/**
 * Recuperación de contraseña por correo (Paso 7.3), para usuarios y administradores.
 * <p>
 * Flujo: la persona pide recuperar su contraseña con su correo, recibe un enlace con un token de un solo uso
 * que vence en 30 minutos y, desde ese enlace, escribe la nueva contraseña. El token no se guarda: solo su
 * hash SHA-256.
 */
public interface RecuperacionServicio {

    /** Minutos de vigencia del enlace de recuperación. */
    int MINUTOS_VIGENCIA = 30;

    /** Mensaje único para un token inexistente, vencido o ya usado (no revela cuál de los casos es). */
    String MENSAJE_ENLACE_INVALIDO = "El enlace no es válido o ya venció";

    /**
     * Pide recuperar la contraseña. El correo se normaliza (sin espacios en los extremos y en minúsculas).
     * Si el correo es de un usuario o administrador: invalida los tokens anteriores sin usar de ese correo,
     * genera uno nuevo y envía el enlace por correo después del commit y en segundo plano. Si no es de nadie,
     * <b>no hace nada y no lanza nada</b>, para no revelar qué correos existen. Si el envío falla, solo se registra
     * el error.
     *
     * @throws NegocioException si el correo viene vacío o con formato inválido
     */
    void solicitarRecuperacion(String email) throws NegocioException;

    /**
     * Valida un token recibido por el enlace.
     *
     * @return el correo de la persona dueña del token
     * @throws NegocioException con el mismo mensaje si el token no existe, venció o ya se usó
     */
    String validarToken(String token) throws NegocioException;

    /**
     * Cambia la contraseña de la persona dueña del token (usuario o administrador) y marca el token como usado,
     * todo en la misma transacción. Un usuario desactivado sí puede restablecerla, pero no podrá iniciar sesión.
     *
     * @throws NegocioException si el token no sirve (mismo mensaje), si la contraseña es obligatoria, tiene menos
     *                          de 6 o más de 100 caracteres, o no coincide con la confirmación
     */
    void restablecerPassword(String token, String nueva, String confirmacion) throws NegocioException;
}
