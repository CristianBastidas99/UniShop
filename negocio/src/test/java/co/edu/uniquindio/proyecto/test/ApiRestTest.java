package co.edu.uniquindio.proyecto.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de la API REST con {@link MockMvc}: códigos HTTP y cuerpo {@code { "mensaje" }} de los errores
 * ({@code ManejadorErroresRest}), el 403 del que no es dueño y el mapeo de usuarios sin {@code @Transactional}
 * en el controlador. Usan los datos de {@code unishop.sql} y la transacción de la prueba (rollback al terminar).
 * <p>
 * {@code MockMvc} no pasa por {@code /error}; las rutas inexistentes se prueban en {@link ApiRutaInexistenteTest}.
 */
@AutoConfigureMockMvc
class ApiRestTest extends BaseServicioTest {

    /** Cuerpo válido para actualizar el producto 1 (Cepillo Electrico, vendedor 5). */
    private static final String PRODUCTO_1 = "{\"nombre\":\"Cepillo Electrico\",\"descripcion\":\"d\",\"unidades\":20,"
            + "\"precio\":30000,\"fechaLimite\":\"2030-07-04T23:59:59\",\"idCiudad\":1,"
            + "\"imagenes\":[\"cepillo.png\"],\"idsCategorias\":[1,3]}";

    @Autowired
    private MockMvc mvc;

    @Test
    void productoInexistenteDa404() throws Exception {
        mvc.perform(get("/api/productos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El producto no existe"));
    }

    @Test
    void actualizarSinSerDuenoDa403() throws Exception {
        mvc.perform(put("/api/productos/1").param("idUsuario", "6")
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el vendedor puede modificar el producto"));
    }

    @Test
    void eliminarSinSerDuenoDa403() throws Exception {
        mvc.perform(delete("/api/productos/1").param("idUsuario", "6"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el vendedor puede eliminar el producto"));
    }

    @Test
    void descuentoSinSerDuenoDa403() throws Exception {
        mvc.perform(patch("/api/productos/1/descuento").param("idUsuario", "6")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"descuento\":0.5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void descuentoDelDuenoFunciona() throws Exception {
        mvc.perform(patch("/api/productos/1/descuento").param("idUsuario", "5")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"descuento\":0.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioConDescuento").value(15000.0));
    }

    @Test
    void reglaDeNegocioSigueDando400() throws Exception {
        mvc.perform(get("/api/productos/buscar").param("precioMin", "500").param("precioMax", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El precio mínimo no puede ser mayor que el máximo"));
    }

    @Test
    void cuerpoInvalidoDa400ConLosCampos() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\",\"precio\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("nombre: ")))
                .andExpect(jsonPath("$.mensaje", containsString("precio: ")));
    }

    @Test
    void jsonMalFormadoDa400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("{ \"nombre\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la petición no es válido"));
    }

    @Test
    void idNoNumericoDa400() throws Exception {
        mvc.perform(get("/api/productos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parámetro 'id' no tiene un valor válido"));
    }

    @Test
    void parametroFaltanteDa400() throws Exception {
        mvc.perform(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parámetro 'idUsuario' es obligatorio"));
    }

    @Test
    void metodoNoPermitidoDa405ConAllow() throws Exception {
        mvc.perform(delete("/api/productos"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.mensaje").value("Método DELETE no permitido en esta ruta"));
    }

    @Test
    void tipoDeContenidoNoSoportadoDa415() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.mensaje", startsWith("Tipo de contenido no soportado")));
    }

    @Test
    void noAceptarJsonDa406SinCuerpo() throws Exception {
        mvc.perform(get("/api/productos/1").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string(""));
    }

    @Test
    void errorConAcceptXmlSaleIgualEnJson() throws Exception {
        mvc.perform(get("/api/productos/999999").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("El producto no existe"));
    }

    @Test
    void usuarioConTelefonosYCiudadSinPassword() throws Exception {
        mvc.perform(get("/api/usuarios/lorena@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefonos", hasSize(2)))
                .andExpect(jsonPath("$.nombreCiudad").value("Armenia"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void listaDeUsuariosSinDuplicados() throws Exception {
        // Lorena tiene 2 teléfonos: si el mapeo hiciera join con los teléfonos, saldría 2 veces
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'lorena@email.com')]", hasSize(1)));
    }
}
