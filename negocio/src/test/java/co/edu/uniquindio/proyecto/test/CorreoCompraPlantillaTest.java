package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.correo.CorreoPendienteEvent;
import co.edu.uniquindio.proyecto.dto.ItemCompraDTO;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.servicios.CompraServicio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verifica el correo que arma y publica cada compra. Las pruebas hacen rollback, por eso el evento AFTER_COMMIT
 * no se dispara: solo se revisa el contenido del evento publicado.
 */
@RecordApplicationEvents
public class CorreoCompraPlantillaTest extends BaseServicioTest {

    @Autowired
    private CompraServicio compraServicio;

    @Autowired
    private ProductoRepo productoRepo;

    @Autowired
    private ApplicationEvents eventos;

    private CorreoPendienteEvent unicoCorreo() {
        List<CorreoPendienteEvent> correos = eventos.stream(CorreoPendienteEvent.class).collect(Collectors.toList());
        Assertions.assertEquals(1, correos.size());
        return correos.get(0);
    }

    @Test
    public void comprarPublicaCorreoConElDetalleTest() throws NegocioException {
        long id = compraServicio.comprar(7L,
                Arrays.asList(new ItemCompraDTO(1L, 2), new ItemCompraDTO(4L, 1)), MedioPago.PSE, null).getId();

        CorreoPendienteEvent correo = unicoCorreo();
        Assertions.assertEquals("pepe@email.com", correo.getDestinatario());
        Assertions.assertEquals("Tu compra #" + id + " en UniShop", correo.getAsunto());
        String html = correo.getHtml();
        Assertions.assertTrue(html.contains("Hola Pepe Perez"));
        Assertions.assertTrue(html.contains("Cepillo Electrico"));
        Assertions.assertTrue(html.contains("Licuadora"));
        Assertions.assertTrue(html.contains("$ 21.000"));
        Assertions.assertTrue(html.contains("$ 42.000"));
        Assertions.assertTrue(html.contains("Total: $ 192.000"));
        Assertions.assertTrue(html.contains("PSE"));
        Assertions.assertTrue(html.matches("(?s).*\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}.*"));
        Assertions.assertFalse(html.contains("Cupón"));
        Assertions.assertFalse(html.contains("ganaste la subasta"));
    }

    @Test
    public void comprarConCuponMuestraCuponYDescuentoTest() throws NegocioException {
        compraServicio.comprar(7L,
                Arrays.asList(new ItemCompraDTO(1L, 2), new ItemCompraDTO(4L, 1)), MedioPago.TARJETA, "NAVIDAD20");

        String html = unicoCorreo().getHtml();
        Assertions.assertTrue(html.contains("Cupón NAVIDAD20"));
        Assertions.assertTrue(html.contains("Subtotal: $ 192.000"));
        Assertions.assertTrue(html.contains("-$ 38.400"));
        Assertions.assertTrue(html.contains("Total: $ 153.600"));
        Assertions.assertTrue(html.contains("Tarjeta"));
    }

    @Test
    public void comprarEnSubastaIncluyeNotaDeSubastaTest() throws NegocioException {
        compraServicio.comprarEnSubasta(7L, 3L, 3500.0);

        CorreoPendienteEvent correo = unicoCorreo();
        Assertions.assertEquals("pepe@email.com", correo.getDestinatario());
        Assertions.assertTrue(correo.getHtml().contains("ganaste la subasta"));
        Assertions.assertTrue(correo.getHtml().contains("Juguete Perro"));
        Assertions.assertTrue(correo.getHtml().contains("Total: $ 3.500"));
    }

    @Test
    public void nombreDeProductoSeEscapaTest() throws NegocioException {
        Producto producto = productoRepo.findById(1L).orElseThrow(AssertionError::new);
        producto.setNombre("<script>alert(1)</script>");
        productoRepo.save(producto);

        compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, 1)), MedioPago.PAYPAL, null);

        String html = unicoCorreo().getHtml();
        Assertions.assertFalse(html.contains("<script>"));
        Assertions.assertTrue(html.contains("&lt;script&gt;"));
    }

    @Test
    public void compraFallidaNoPublicaCorreoTest() {
        Assertions.assertThrows(NegocioException.class, () -> compraServicio.comprar(7L,
                Collections.singletonList(new ItemCompraDTO(1L, 999)), MedioPago.PSE, null));
        Assertions.assertEquals(0, eventos.stream(CorreoPendienteEvent.class).count());
    }
}
