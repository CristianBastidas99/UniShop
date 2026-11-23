package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.correo.CorreoPendienteEvent;
import co.edu.uniquindio.proyecto.entidades.TokenRecuperacion;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.TokenRecuperacionRepo;
import co.edu.uniquindio.proyecto.servicios.AdministradorServicio;
import co.edu.uniquindio.proyecto.servicios.EmailServicio;
import co.edu.uniquindio.proyecto.servicios.RecuperacionServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Pruebas de la recuperación de contraseña. El correo se publica como {@link CorreoPendienteEvent} y se envía
 * después del commit; como estas pruebas hacen rollback, el envío real nunca ocurre: se verifica el evento
 * publicado y que {@link EmailServicio} no se llama dentro de la transacción.
 */
@RecordApplicationEvents
public class RecuperacionServicioTest extends BaseServicioTest {

    private static final String MENSAJE_INVALIDO = "El enlace no es válido o ya venció";
    private static final Pattern ENLACE = Pattern.compile("(http[^\"<\\s]*/restablecerPassword\\.xhtml\\?token=([A-Za-z0-9_-]+))");

    @Autowired
    private RecuperacionServicio recuperacionServicio;
    @Autowired
    private UsuarioServicio usuarioServicio;
    @Autowired
    private AdministradorServicio administradorServicio;
    @Autowired
    private TokenRecuperacionRepo tokenRepo;

    @MockBean
    private EmailServicio emailServicio;

    @Autowired
    private ApplicationEvents eventos;

    private List<CorreoPendienteEvent> correos() {
        return eventos.stream(CorreoPendienteEvent.class).collect(Collectors.toList());
    }

    /** Pide la recuperación y devuelve el HTML del correo enviado. */
    private String pedirYCapturarHtml(String email) throws NegocioException {
        recuperacionServicio.solicitarRecuperacion(email);
        sincronizar();
        List<CorreoPendienteEvent> correos = correos();
        assertFalse(correos.isEmpty(), "Debe publicar el correo");
        CorreoPendienteEvent ultimo = correos.get(correos.size() - 1);
        assertEquals(email.trim().toLowerCase(), ultimo.getDestinatario());
        return ultimo.getHtml();
    }

    private String tokenDe(String html) {
        Matcher m = ENLACE.matcher(html);
        assertTrue(m.find(), "El correo debe traer el enlace");
        return m.group(2);
    }

    private String pedirToken(String email) throws NegocioException {
        return tokenDe(pedirYCapturarHtml(email));
    }

    private void assertEnlaceInvalido(String token) {
        NegocioException e = assertThrows(NegocioException.class, () -> recuperacionServicio.validarToken(token));
        assertEquals(MENSAJE_INVALIDO, e.getMessage());
    }

    private List<TokenRecuperacion> tokensDe(String email) {
        return tokenRepo.findAll().stream().filter(t -> t.getEmail().equals(email)).collect(Collectors.toList());
    }

    @Test
    public void solicitarUsuarioEnviaCorreoConEnlaceTest() throws NegocioException {
        String html = pedirYCapturarHtml("marta@email.com");

        Matcher m = ENLACE.matcher(html);
        assertTrue(m.find());
        assertTrue(m.group(1).startsWith("http://localhost:8080/restablecerPassword.xhtml?token="));
        assertTrue(html.contains("Marta Wayne"));
        assertTrue(html.contains("30 minutos"));
        assertEquals(43, m.group(2).length()); // 32 bytes en Base64 URL sin relleno
        assertEquals("marta@email.com", recuperacionServicio.validarToken(m.group(2)));
    }

    @Test
    public void solicitarAdministradorEnviaCorreoTest() throws NegocioException {
        String token = pedirToken("profesor@uniquindio.com");
        assertEquals("profesor@uniquindio.com", recuperacionServicio.validarToken(token));
    }

    @Test
    public void solicitarNormalizaCorreoTest() throws NegocioException {
        String token = tokenDe(pedirYCapturarHtml("  MARTA@Email.com "));
        assertEquals("marta@email.com", recuperacionServicio.validarToken(token));
    }

    @Test
    public void enBaseSoloQuedaElHashTest() throws NegocioException {
        String token = pedirToken("marta@email.com");
        List<TokenRecuperacion> tokens = tokensDe("marta@email.com");

        assertEquals(1, tokens.size());
        TokenRecuperacion t = tokens.get(0);
        assertNotEquals(token, t.getTokenHash());
        assertEquals(64, t.getTokenHash().length());
        assertTrue(t.getTokenHash().matches("[0-9a-f]{64}"));
        assertFalse(t.isUsado());
        assertNull(t.getFechaUso());
        long minutos = (t.getFechaExpiracion().getTime() - t.getFechaCreacion().getTime()) / 60000;
        assertEquals(30, minutos);
    }

    @Test
    public void correoInexistenteNoEnviaNiLanzaTest() throws NegocioException {
        long antes = tokenRepo.count();
        recuperacionServicio.solicitarRecuperacion("nadie@email.com");
        sincronizar();

        assertTrue(correos().isEmpty());
        assertEquals(antes, tokenRepo.count());
    }

    @Test
    public void correoInvalidoLanzaExcepcionTest() {
        assertThrows(NegocioException.class, () -> recuperacionServicio.solicitarRecuperacion(null));
        assertThrows(NegocioException.class, () -> recuperacionServicio.solicitarRecuperacion("   "));
        assertThrows(NegocioException.class, () -> recuperacionServicio.solicitarRecuperacion("no-es-correo"));
        assertThrows(NegocioException.class, () -> recuperacionServicio.solicitarRecuperacion("a@b"));
    }

    @Test
    public void tokenInexistenteVencidoOUsadoDanElMismoMensajeTest() throws NegocioException {
        // Inexistente
        assertEnlaceInvalido("token-que-no-existe");
        assertEnlaceInvalido("");
        assertEnlaceInvalido(null);

        // Vencido
        String vencido = pedirToken("marta@email.com");
        TokenRecuperacion t = tokensDe("marta@email.com").get(0);
        t.setFechaExpiracion(new Date(System.currentTimeMillis() - 1000));
        tokenRepo.save(t);
        sincronizar();
        assertEnlaceInvalido(vencido);

        // Usado
        String usado = pedirToken("pepe@email.com");
        recuperacionServicio.restablecerPassword(usado, "nueva123", "nueva123");
        sincronizar();
        assertEnlaceInvalido(usado);
    }

    @Test
    public void pedirDosVecesInvalidaElPrimeroTest() throws NegocioException {
        String primero = pedirToken("marta@email.com");
        String segundo = pedirToken("marta@email.com");

        assertNotEquals(primero, segundo);
        assertEnlaceInvalido(primero);
        assertEquals("marta@email.com", recuperacionServicio.validarToken(segundo));
        List<TokenRecuperacion> tokens = tokensDe("marta@email.com");
        assertEquals(2, tokens.size());
        assertEquals(1, tokens.stream().filter(t -> !t.isUsado()).count());
        assertTrue(tokens.stream().filter(TokenRecuperacion::isUsado).allMatch(t -> t.getFechaUso() != null));
    }

    @Test
    public void restablecerPasswordUsuarioTest() throws NegocioException {
        String token = pedirToken("marta@email.com");

        recuperacionServicio.restablecerPassword(token, "nueva123", "nueva123");
        sincronizar();

        assertEquals("marta@email.com", usuarioServicio.iniciarSesion("marta@email.com", "nueva123").getEmail());
        assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion("marta@email.com", "12345"));
        TokenRecuperacion t = tokensDe("marta@email.com").get(0);
        assertTrue(t.isUsado());
        assertNotNull(t.getFechaUso());
    }

    @Test
    public void restablecerPasswordAdministradorTest() throws NegocioException {
        String token = pedirToken("profesor@uniquindio.com");

        recuperacionServicio.restablecerPassword(token, "nueva123", "nueva123");
        sincronizar();

        assertEquals("profesor@uniquindio.com",
                administradorServicio.iniciarSesion("profesor@uniquindio.com", "nueva123").getEmail());
        assertThrows(NegocioException.class, () -> administradorServicio.iniciarSesion("profesor@uniquindio.com", "12345"));
    }

    @Test
    public void contraseniaInvalidaNoConsumeElTokenTest() throws NegocioException {
        String token = pedirToken("marta@email.com");
        String largaDeMas = "x".repeat(101);

        assertThrows(NegocioException.class, () -> recuperacionServicio.restablecerPassword(token, "", ""));
        assertThrows(NegocioException.class, () -> recuperacionServicio.restablecerPassword(token, null, null));
        assertThrows(NegocioException.class, () -> recuperacionServicio.restablecerPassword(token, "12345", "12345"));
        assertThrows(NegocioException.class, () -> recuperacionServicio.restablecerPassword(token, largaDeMas, largaDeMas));
        assertThrows(NegocioException.class, () -> recuperacionServicio.restablecerPassword(token, "nueva123", "otra1234"));
        assertThrows(NegocioException.class, () -> recuperacionServicio.restablecerPassword(token, "nueva123", null));
        sincronizar();

        // Sigue sirviendo y la contraseña no cambió
        assertEquals("marta@email.com", recuperacionServicio.validarToken(token));
        assertFalse(tokensDe("marta@email.com").get(0).isUsado());
        assertNotNull(usuarioServicio.iniciarSesion("marta@email.com", "12345"));
    }

    @Test
    public void tokenNoSirveUnaSegundaVezTest() throws NegocioException {
        String token = pedirToken("marta@email.com");
        recuperacionServicio.restablecerPassword(token, "nueva123", "nueva123");
        sincronizar();

        NegocioException e = assertThrows(NegocioException.class,
                () -> recuperacionServicio.restablecerPassword(token, "otraClave1", "otraClave1"));
        assertEquals(MENSAJE_INVALIDO, e.getMessage());
        assertNotNull(usuarioServicio.iniciarSesion("marta@email.com", "nueva123"));
    }

    @Test
    public void usuarioDesactivadoRestableceSinPoderIniciarTest() throws NegocioException {
        usuarioServicio.cambiarActivo("marta@email.com", false);
        sincronizar();
        String token = pedirToken("marta@email.com");

        recuperacionServicio.restablecerPassword(token, "nueva123", "nueva123");
        sincronizar();

        assertThrows(NegocioException.class, () -> usuarioServicio.iniciarSesion("marta@email.com", "nueva123"));
    }

    @Test
    public void correoSeEnviaDespuesDelCommitTest() throws NegocioException {
        // Dentro de la transacción solo se publica el evento: el envío (CorreoPendienteListener, AFTER_COMMIT y
        // en segundo plano) no ocurre todavía. Así la respuesta no depende del servidor de correo.
        recuperacionServicio.solicitarRecuperacion("marta@email.com");
        sincronizar();

        assertEquals(1, correos().size());
        assertEquals("Recupera tu contraseña de UniShop", correos().get(0).getAsunto());
        Mockito.verify(emailServicio, Mockito.never()).enviarCorreo(anyString(), anyString(), anyString());
        assertEquals(1, tokensDe("marta@email.com").size());
    }
}
