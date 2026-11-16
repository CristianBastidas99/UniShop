package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.HistorialPrecio;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.servicios.HistorialPrecioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistorialPrecioServicioTest extends BaseServicioTest {

    @Autowired
    private HistorialPrecioServicio historialServicio;

    @Autowired
    private ProductoRepo productoRepo;

    private HistorialPrecio nuevoHistorial(Long idProducto, Double precioNuevo) {
        Producto producto = productoRepo.findById(idProducto).orElseThrow(AssertionError::new);
        HistorialPrecio h = new HistorialPrecio();
        h.setPrecioAnterior(30000.0);
        h.setPrecioNuevo(precioNuevo);
        h.setDescuentoAnterior(0.3);
        h.setDescuentoNuevo(0.2);
        h.setProducto(producto);
        return h;
    }

    @Test
    public void registrarHistorialTest() throws Exception {
        Date antes = new Date();
        HistorialPrecio guardado = historialServicio.registrarHistorial(nuevoHistorial(1L, 28000.0));
        sincronizar();
        assertNotNull(guardado.getId());
        HistorialPrecio leido = historialServicio.obtenerHistorial(guardado.getId());
        assertEquals(28000.0, leido.getPrecioNuevo());
        assertNotNull(leido.getFecha());
        // MySQL puede redondear los milisegundos, por eso se compara con un margen de un segundo.
        assertTrue(leido.getFecha().getTime() >= antes.getTime() - 1000);
    }

    @Test
    public void registrarConFechaInformadaTest() throws Exception {
        HistorialPrecio h = nuevoHistorial(1L, 28000.0);
        Date fecha = new Date(1700000000000L);
        h.setFecha(fecha);
        HistorialPrecio guardado = historialServicio.registrarHistorial(h);
        sincronizar();
        assertEquals(fecha.getTime() / 1000, historialServicio.obtenerHistorial(guardado.getId()).getFecha().getTime() / 1000);
    }

    @Test
    public void registrarConIdExistenteTest() {
        HistorialPrecio h = nuevoHistorial(1L, 28000.0);
        h.setId(1L);
        NegocioException e = assertThrows(NegocioException.class, () -> historialServicio.registrarHistorial(h));
        assertEquals("El historial de precio ya existe", e.getMessage());
    }

    @Test
    public void actualizarHistorialTest() throws Exception {
        HistorialPrecio h = historialServicio.obtenerHistorial(1L);
        h.setPrecioNuevo(29000.0);
        historialServicio.actualizarHistorial(h);
        sincronizar();
        assertEquals(29000.0, historialServicio.obtenerHistorial(1L).getPrecioNuevo());
    }

    @Test
    public void actualizarConIdNuloTest() {
        assertThrows(NegocioException.class, () -> historialServicio.actualizarHistorial(nuevoHistorial(1L, 1000.0)));
    }

    @Test
    public void actualizarInexistenteTest() {
        HistorialPrecio h = nuevoHistorial(1L, 1000.0);
        h.setId(99999L);
        assertThrows(NegocioException.class, () -> historialServicio.actualizarHistorial(h));
    }

    @Test
    public void eliminarHistorialTest() throws Exception {
        historialServicio.eliminarHistorial(1L);
        sincronizar();
        assertThrows(NegocioException.class, () -> historialServicio.obtenerHistorial(1L));
    }

    @Test
    public void eliminarInexistenteTest() {
        assertThrows(NegocioException.class, () -> historialServicio.eliminarHistorial(99999L));
    }

    @Test
    public void eliminarNuloTest() {
        assertThrows(NegocioException.class, () -> historialServicio.eliminarHistorial(null));
    }

    @Test
    public void listarHistorialesTest() {
        assertEquals(3, historialServicio.listarHistoriales().size());
    }

    @Test
    public void obtenerInexistenteTest() {
        assertThrows(NegocioException.class, () -> historialServicio.obtenerHistorial(99999L));
    }

    @Test
    public void obtenerNuloTest() {
        assertThrows(NegocioException.class, () -> historialServicio.obtenerHistorial(null));
    }

    @Test
    public void listarPorProductoTest() throws Exception {
        assertEquals(1, historialServicio.listarPorProducto(1L).size());
        assertEquals(0, historialServicio.listarPorProducto(3L).size());
    }

    @Test
    public void listarPorProductoOrdenadoTest() throws Exception {
        HistorialPrecio reciente = historialServicio.registrarHistorial(nuevoHistorial(1L, 28000.0));
        sincronizar();
        List<HistorialPrecio> lista = historialServicio.listarPorProducto(1L);
        assertEquals(2, lista.size());
        // El más reciente primero y el de los datos de prueba (2 de enero de 2026) al final.
        assertEquals(reciente.getId(), lista.get(0).getId());
        assertEquals(1L, lista.get(1).getId());
    }

    @Test
    public void listarPorProductoInexistenteTest() {
        assertThrows(NegocioException.class, () -> historialServicio.listarPorProducto(99999L));
    }

    @Test
    public void listarPorProductoNuloTest() {
        assertThrows(NegocioException.class, () -> historialServicio.listarPorProducto(null));
    }
}
