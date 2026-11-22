package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.NegocioApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rutas que no existen, con servidor real: solo así la petición pasa por {@code /error} de Spring Boot
 * ({@code MockMvc} no lo hace). Verifica {@code ErroresApiAtributos}: las rutas {@code /api} responden
 * {@code { "mensaje" }} y las demás conservan el formato por defecto.
 * <p>
 * Solo hace GET: con servidor real no hay rollback, así que no debe escribir en la base.
 */
@SpringBootTest(classes = NegocioApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "unishop.tareas.habilitadas=false")
class ApiRutaInexistenteTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    @SuppressWarnings("rawtypes")
    void rutaApiInexistenteDaMensaje() {
        ResponseEntity<Map> respuesta = rest.getForEntity("/api/nada", Map.class);
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals(Map.of("mensaje", "La ruta /api/nada no existe"), respuesta.getBody());
    }

    @Test
    @SuppressWarnings("rawtypes")
    void rutaFueraDeLaApiConservaElFormatoPorDefecto() {
        ResponseEntity<Map> respuesta = rest.getForEntity("/otra-ruta", Map.class);
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(404, respuesta.getBody().get("status"));
        assertFalse(respuesta.getBody().containsKey("mensaje"));
    }
}
