package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.CantidadDTO;
import co.edu.uniquindio.proyecto.dto.ItemCompraDTO;
import co.edu.uniquindio.proyecto.dto.MedioPagoDTO;
import co.edu.uniquindio.proyecto.dto.PromedioDTO;
import co.edu.uniquindio.proyecto.dto.VentaMesDTO;
import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.CompraServicio;
import co.edu.uniquindio.proyecto.servicios.ReporteServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReporteServicioTest extends BaseServicioTest {

    @Autowired
    private ReporteServicio reporteServicio;

    @Autowired
    private CompraServicio compraServicio;

    @Autowired
    private ComentarioServicio comentarioServicio;

    @Test
    public void productosPorCategoriaTest() {
        assertEquals(Arrays.asList(
                new CantidadDTO("Aseo", 1L),
                new CantidadDTO("Cocina", 1L),
                new CantidadDTO("Hogar", 2L),
                new CantidadDTO("Mascotas", 1L),
                new CantidadDTO("Tecnologia", 3L)), reporteServicio.productosPorCategoria());
    }

    @Test
    public void ventasPorMesTest() {
        assertEquals(Arrays.asList(
                new VentaMesDTO(2026, 1, 40500.0),
                new VentaMesDTO(2026, 2, 270000.0),
                new VentaMesDTO(2026, 3, 2510000.0)), reporteServicio.ventasPorMes());
    }

    @Test
    public void topVendedoresTest() throws NegocioException {
        List<CantidadDTO> esperado = Arrays.asList(
                new CantidadDTO("Lorena Lopez", 3L),
                new CantidadDTO("Marta Wayne", 1L),
                new CantidadDTO("Pedro Martinez", 1L),
                new CantidadDTO("Pepe Perez", 1L));
        assertEquals(esperado, reporteServicio.topVendedores(5));
        assertEquals(esperado, reporteServicio.topVendedores());
        assertEquals(Collections.singletonList(new CantidadDTO("Lorena Lopez", 3L)),
                reporteServicio.topVendedores(1));
    }

    @Test
    public void mejorCalificadosTest() throws NegocioException {
        List<PromedioDTO> esperado = Arrays.asList(
                new PromedioDTO("Juguete Perro", 5.0),
                new PromedioDTO("Cepillo Electrico", 4.5),
                new PromedioDTO("Portatil", 4.0),
                new PromedioDTO("SmartPhone", 3.0));
        assertEquals(esperado, reporteServicio.mejorCalificados(5));
        assertEquals(esperado, reporteServicio.mejorCalificados());
        assertEquals(esperado.subList(0, 2), reporteServicio.mejorCalificados(2));
    }

    @Test
    public void comprasPorMedioPagoTest() {
        assertEquals(Arrays.asList(
                new MedioPagoDTO(MedioPago.BALOTO, 1L),
                new MedioPagoDTO(MedioPago.PAYPAL, 1L),
                new MedioPagoDTO(MedioPago.PSE, 1L),
                new MedioPagoDTO(MedioPago.TARJETA, 1L)), reporteServicio.comprasPorMedioPago());
    }

    @Test
    public void limiteInvalidoTest() {
        for (int limite : new int[]{0, 51}) {
            NegocioException e1 = assertThrows(NegocioException.class, () -> reporteServicio.topVendedores(limite));
            assertEquals("El límite debe estar entre 1 y 50", e1.getMessage());
            NegocioException e2 = assertThrows(NegocioException.class, () -> reporteServicio.mejorCalificados(limite));
            assertEquals("El límite debe estar entre 1 y 50", e2.getMessage());
        }
    }

    @Test
    public void compraRealSeReflejaEnReportesTest() throws NegocioException {
        // Cepillo (30000, 30 % de descuento) x2 con NAVIDAD20: 30000 * 0.7 * 0.8 = 16800 por unidad
        compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, 2)), MedioPago.PSE, "NAVIDAD20");
        sincronizar();

        LocalDate hoy = LocalDate.now();
        List<VentaMesDTO> ventas = reporteServicio.ventasPorMes();
        assertEquals(4, ventas.size());
        assertEquals(new VentaMesDTO(hoy.getYear(), hoy.getMonthValue(), 33600.0), ventas.get(3));

        assertEquals(Arrays.asList(
                new MedioPagoDTO(MedioPago.PSE, 2L),
                new MedioPagoDTO(MedioPago.BALOTO, 1L),
                new MedioPagoDTO(MedioPago.PAYPAL, 1L),
                new MedioPagoDTO(MedioPago.TARJETA, 1L)), reporteServicio.comprasPorMedioPago());

        assertEquals(new CantidadDTO("Lorena Lopez", 5L), reporteServicio.topVendedores(1).get(0));
    }

    @Test
    public void comentarioNuevoCambiaMejorCalificadosTest() throws NegocioException {
        // Lorena (5) califica con 0 el Portatil: promedio (4 + 0) / 2 = 2.0
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        Producto producto = new Producto();
        producto.setId(5L);
        Comentario comentario = new Comentario();
        comentario.setUsuario(usuario);
        comentario.setProducto(producto);
        comentario.setMensaje("Se calienta mucho");
        comentario.setCalificacion(0);
        comentarioServicio.comentarProducto(comentario);
        sincronizar();

        assertEquals(Arrays.asList(
                new PromedioDTO("Juguete Perro", 5.0),
                new PromedioDTO("Cepillo Electrico", 4.5),
                new PromedioDTO("SmartPhone", 3.0),
                new PromedioDTO("Portatil", 2.0)), reporteServicio.mejorCalificados());
    }
}
