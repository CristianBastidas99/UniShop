package co.edu.uniquindio.proyecto.seguridad;

import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Utilidades comunes para las contraseñas: el límite de BCrypt y el reconocimiento de hashes BCrypt.
 */
public final class Contrasenas {

    /** BCrypt solo usa los primeros 72 bytes de la contraseña; el resto se ignoraría sin avisar. */
    public static final int MAX_BYTES = 72;

    private static final Pattern FORMATO_BCRYPT = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private Contrasenas() {
    }

    /**
     * Valida que la contraseña no pase de 72 bytes en UTF-8 (límite de BCrypt).
     *
     * @throws NegocioException si la contraseña es demasiado larga
     */
    public static void validarLongitud(String password) throws NegocioException {
        if (password != null && password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new NegocioException("La contraseña es demasiado larga: no puede superar " + MAX_BYTES
                    + " bytes (con tildes o símbolos especiales ocupa menos caracteres)");
        }
    }

    /** Indica si el texto ya tiene formato de hash BCrypt (por ejemplo {@code $2a$10$...}). */
    public static boolean esBcrypt(String texto) {
        return texto != null && FORMATO_BCRYPT.matcher(texto).matches();
    }
}
