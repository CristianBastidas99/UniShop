package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.correo.CorreoPendienteEvent;
import co.edu.uniquindio.proyecto.entidades.TokenRecuperacion;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.TokenRecuperacionRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class RecuperacionServicioImpl implements RecuperacionServicio {

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final int BYTES_TOKEN = 32;
    private static final int PASSWORD_MIN = 6;
    private static final int PASSWORD_MAX = 100;
    private static final String FORMATO_EMAIL = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";

    private final TokenRecuperacionRepo tokenRepo;
    private final UsuarioRepo usuarioRepo;
    private final AdministradorRepo administradorRepo;
    private final UsuarioServicio usuarioServicio;
    private final AdministradorServicio administradorServicio;
    private final ApplicationEventPublisher publicador;
    private final String urlBase;

    public RecuperacionServicioImpl(TokenRecuperacionRepo tokenRepo, UsuarioRepo usuarioRepo,
                                    AdministradorRepo administradorRepo, UsuarioServicio usuarioServicio,
                                    AdministradorServicio administradorServicio, ApplicationEventPublisher publicador,
                                    @Value("${unishop.url-base:http://localhost:8080}") String urlBase) {
        this.tokenRepo = tokenRepo;
        this.usuarioRepo = usuarioRepo;
        this.administradorRepo = administradorRepo;
        this.usuarioServicio = usuarioServicio;
        this.administradorServicio = administradorServicio;
        this.publicador = publicador;
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    /** SHA-256 en hexadecimal (64 caracteres). */
    private static String hash(String token) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : resumen) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    @Override
    @Transactional(rollbackFor = NegocioException.class)
    public void solicitarRecuperacion(String email) throws NegocioException {
        if (email == null || email.trim().isEmpty()) {
            throw new NegocioException("El correo es obligatorio");
        }
        String correo = email.trim().toLowerCase(Locale.ROOT);
        if (correo.length() > 100 || !correo.matches(FORMATO_EMAIL)) {
            throw new NegocioException("El correo no tiene un formato válido");
        }
        String nombre;
        if (usuarioRepo.findByEmail(correo).isPresent()) {
            nombre = usuarioRepo.findByEmail(correo).get().getNombre();
        } else if (administradorRepo.findByEmail(correo).isPresent()) {
            nombre = administradorRepo.findByEmail(correo).get().getNombre();
        } else {
            return; // No revela que el correo no existe
        }

        Date ahora = new Date();
        // Invalida los tokens anteriores sin usar: solo sirve el último enlace
        List<TokenRecuperacion> anteriores = tokenRepo.findByEmailAndUsadoFalse(correo);
        for (TokenRecuperacion anterior : anteriores) {
            anterior.setUsado(true);
            anterior.setFechaUso(ahora);
        }
        tokenRepo.saveAll(anteriores);

        byte[] bytes = new byte[BYTES_TOKEN];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        TokenRecuperacion nuevo = new TokenRecuperacion();
        nuevo.setTokenHash(hash(token)); // Solo se guarda el hash
        nuevo.setEmail(correo);
        nuevo.setFechaCreacion(ahora);
        nuevo.setFechaExpiracion(new Date(ahora.getTime() + MINUTOS_VIGENCIA * 60L * 1000L));
        tokenRepo.save(nuevo);

        // El correo se envía después del commit y en segundo plano (CorreoPendienteListener), igual que el de
        // las compras: la respuesta tarda lo mismo exista o no el correo (no revela cuáles están registrados),
        // no retiene la conexión a la base durante el envío y, si el envío falla, solo se registra el error.
        publicador.publishEvent(new CorreoPendienteEvent(correo, "Recupera tu contraseña de UniShop",
                armarCorreo(nombre, token)));
    }

    private String armarCorreo(String nombre, String token) {
        String enlace = urlBase + "/restablecerPassword.xhtml?token=" + token;
        return "<div style=\"font-family:Arial,sans-serif;max-width:520px;margin:auto;color:#222\">"
                + "<h2 style=\"color:#1565c0\">Recupera tu contraseña</h2>"
                + "<p>Hola, " + HtmlUtils.htmlEscape(nombre == null ? "" : nombre) + ".</p>"
                + "<p>Recibimos una solicitud para restablecer la contraseña de tu cuenta de UniShop. "
                + "Para elegir una nueva, haz clic en el botón:</p>"
                + "<p style=\"text-align:center\"><a href=\"" + enlace + "\" style=\"background:#1565c0;color:#fff;"
                + "padding:12px 20px;border-radius:4px;text-decoration:none;display:inline-block\">"
                + "Restablecer contraseña</a></p>"
                + "<p>Si el botón no funciona, copia este enlace en tu navegador:<br>" + enlace + "</p>"
                + "<p><b>El enlace vence en " + MINUTOS_VIGENCIA + " minutos</b> y solo se puede usar una vez.</p>"
                + "<p style=\"color:#667\">Si no pediste este cambio, puedes ignorar este correo: tu contraseña "
                + "seguirá igual.</p>"
                + "<p>UniShop</p></div>";
    }

    /** Busca el token vigente (no usado y sin vencer) por su hash, o lanza el mensaje único de enlace inválido. */
    private TokenRecuperacion buscarVigente(String token) throws NegocioException {
        if (token == null || token.trim().isEmpty()) {
            throw new NegocioException(MENSAJE_ENLACE_INVALIDO);
        }
        TokenRecuperacion t = tokenRepo.findByTokenHash(hash(token.trim()))
                .orElseThrow(() -> new NegocioException(MENSAJE_ENLACE_INVALIDO));
        if (t.isUsado() || !t.getFechaExpiracion().after(new Date())) {
            throw new NegocioException(MENSAJE_ENLACE_INVALIDO);
        }
        return t;
    }

    @Override
    @Transactional(readOnly = true)
    public String validarToken(String token) throws NegocioException {
        return buscarVigente(token).getEmail();
    }

    @Override
    @Transactional(rollbackFor = NegocioException.class)
    public void restablecerPassword(String token, String nueva, String confirmacion) throws NegocioException {
        TokenRecuperacion t = buscarVigente(token);
        if (nueva == null || nueva.isEmpty()) {
            throw new NegocioException("La contraseña es obligatoria");
        }
        if (nueva.length() < PASSWORD_MIN || nueva.length() > PASSWORD_MAX) {
            throw new NegocioException("La contraseña debe tener entre " + PASSWORD_MIN + " y " + PASSWORD_MAX
                    + " caracteres");
        }
        if (!nueva.equals(confirmacion)) {
            throw new NegocioException("La confirmación no coincide con la contraseña");
        }
        if (usuarioRepo.findByEmail(t.getEmail()).isPresent()) {
            usuarioServicio.cambiarPassword(t.getEmail(), nueva);
        } else if (administradorRepo.findByEmail(t.getEmail()).isPresent()) {
            administradorServicio.cambiarPassword(t.getEmail(), nueva);
        } else {
            throw new NegocioException(MENSAJE_ENLACE_INVALIDO);
        }
        t.setUsado(true);
        t.setFechaUso(new Date());
        tokenRepo.save(t);
    }
}
