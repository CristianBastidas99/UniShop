package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.servicios.CompraServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaUsuarioServicio;
import co.edu.uniquindio.proyecto.tareas.CierreSubastasTarea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del flujo de subastas: crear, pujar, cerrar y la tarea programada. */
public class SubastaFlujoServicioTest extends BaseServicioTest {

    @Autowired
    private SubastaServicio subastaServicio;
    @Autowired
    private SubastaUsuarioServicio pujaServicio;
    @Autowired
    private CompraServicio compraServicio;
    @Autowired
    private SubastaRepo subastaRepo;
    @Autowired
    private ProductoRepo productoRepo;
    @Autowired
    private CierreSubastasTarea tarea;

    private String mensaje(org.junit.jupiter.api.function.Executable e) {
        return assertThrows(NegocioException.class, e).getMessage();
    }

    /** Pone la fecha límite de la subasta en el pasado para simular que venció. */
    private void vencer(Long idSubasta) {
        Subasta s = subastaRepo.findById(idSubasta).get();
        s.setFechaLimite(new Date(System.currentTimeMillis() - 60_000));
        subastaRepo.save(s);
        sincronizar();
    }

    // ===== crearSubasta =====

    @Test
    public void crearSubastaTest() throws NegocioException {
        long antes = System.currentTimeMillis();
        Subasta s = subastaServicio.crearSubasta(1L, 5L, 24);
        sincronizar();
        Subasta guardada = subastaRepo.findById(s.getId()).get();
        assertFalse(guardada.isCerrada());
        assertNull(guardada.getCompra());
        long esperado = antes + 24L * 3_600_000L;
        assertTrue(Math.abs(guardada.getFechaLimite().getTime() - esperado) < 60_000);
    }

    @Test
    public void crearSubastaOtroVendedorTest() {
        assertEquals("Solo el vendedor puede subastar su producto",
                mensaje(() -> subastaServicio.crearSubasta(1L, 6L, 24)));
    }

    @Test
    public void crearSubastaProductoVencidoTest() {
        assertThrows(NegocioException.class, () -> subastaServicio.crearSubasta(6L, 6L, 24));
    }

    @Test
    public void crearSubastaSinUnidadesTest() {
        Producto p = productoRepo.findById(1L).get();
        p.setUnidades(0);
        productoRepo.save(p);
        sincronizar();
        assertThrows(NegocioException.class, () -> subastaServicio.crearSubasta(1L, 5L, 24));
    }

    @Test
    public void crearSubastaHorasInvalidasTest() {
        assertEquals("La duración debe ser de al menos una hora",
                mensaje(() -> subastaServicio.crearSubasta(1L, 5L, 0)));
    }

    @Test
    public void crearSubastaPasaFechaProductoTest() {
        assertEquals("La subasta no puede terminar después de la fecha límite del producto",
                mensaje(() -> subastaServicio.crearSubasta(1L, 5L, 1_000_000)));
    }

    @Test
    public void crearSubastaYaActivaTest() {
        assertEquals("El producto ya tiene una subasta activa",
                mensaje(() -> subastaServicio.crearSubasta(5L, 8L, 24)));
    }

    @Test
    public void crearSubastaProductoInexistenteTest() {
        assertThrows(NegocioException.class, () -> subastaServicio.crearSubasta(99999L, 5L, 24));
    }

    // ===== pujar =====

    @Test
    public void pujarTest() throws NegocioException {
        SubastaUsuario puja = pujaServicio.pujar(1L, 7L, 2200000);
        sincronizar();
        assertNotNull(puja.getId());
        assertEquals(2200000.0, pujaServicio.obtenerPujaMasAlta(1L));
    }

    @Test
    public void pujarIgualALaMasAltaTest() {
        assertEquals("La puja debe ser mayor a $2100000", mensaje(() -> pujaServicio.pujar(1L, 7L, 2100000)));
    }

    @Test
    public void pujarMenorALaMasAltaTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.pujar(1L, 7L, 2000000));
    }

    @Test
    public void pujarVendedorTest() {
        assertEquals("El vendedor no puede pujar en su propia subasta",
                mensaje(() -> pujaServicio.pujar(1L, 8L, 3000000)));
    }

    @Test
    public void pujarSubastaVencidaTest() {
        assertEquals("La subasta no está activa", mensaje(() -> pujaServicio.pujar(3L, 7L, 100000)));
    }

    @Test
    public void pujarSubastaCerradaTest() throws NegocioException {
        Subasta s = subastaRepo.findById(1L).get();
        s.setCerrada(true);
        subastaRepo.save(s);
        sincronizar();
        assertEquals("La subasta no está activa", mensaje(() -> pujaServicio.pujar(1L, 7L, 3000000)));
    }

    @Test
    public void pujarUsuarioInexistenteTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.pujar(1L, 99999L, 3000000));
    }

    @Test
    public void pujarSubastaInexistenteTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.pujar(99999L, 7L, 3000000));
    }

    @Test
    public void primeraPujaTest() throws NegocioException {
        Subasta nueva = subastaServicio.crearSubasta(1L, 5L, 24);
        sincronizar();
        assertThrows(NegocioException.class, () -> pujaServicio.pujar(nueva.getId(), 7L, 0));
        pujaServicio.pujar(nueva.getId(), 7L, 100);
        sincronizar();
        assertEquals(100.0, pujaServicio.obtenerPujaMasAlta(nueva.getId()));
    }

    // ===== cerrarSubasta =====

    @Test
    public void cerrarSubastaConPujasTest() throws NegocioException {
        int comprasAntes = compraServicio.listarComprasUsuario(6L).size();
        vencer(1L);

        subastaServicio.cerrarSubasta(1L);
        sincronizar();

        Subasta cerrada = subastaRepo.findById(1L).get();
        assertTrue(cerrada.isCerrada());
        assertNotNull(cerrada.getCompra());
        Compra compra = cerrada.getCompra();
        assertEquals(6L, compra.getUsuario().getId());
        List<DetalleCompra> detalles = compraServicio.listarDetalles(compra.getId());
        assertEquals(1, detalles.size());
        assertEquals(1, detalles.get(0).getUnidades());
        assertEquals(2100000.0, detalles.get(0).getPrecioProducto());
        assertEquals(1, productoRepo.findById(5L).get().getUnidades());
        assertEquals(comprasAntes + 1, compraServicio.listarComprasUsuario(6L).size());
    }

    @Test
    public void cerrarSubastaSinPujasTest() throws NegocioException {
        subastaServicio.cerrarSubasta(3L);
        sincronizar();
        Subasta cerrada = subastaRepo.findById(3L).get();
        assertTrue(cerrada.isCerrada());
        assertNull(cerrada.getCompra());
        assertEquals(4, productoRepo.findById(6L).get().getUnidades());
    }

    @Test
    public void cerrarSubastaYaCerradaTest() throws NegocioException {
        subastaServicio.cerrarSubasta(3L);
        sincronizar();
        assertEquals("La subasta ya está cerrada", mensaje(() -> subastaServicio.cerrarSubasta(3L)));
    }

    @Test
    public void cerrarSubastaActivaTest() {
        assertEquals("La subasta todavía está activa", mensaje(() -> subastaServicio.cerrarSubasta(1L)));
    }

    @Test
    public void cerrarSubastaInexistenteTest() {
        assertThrows(NegocioException.class, () -> subastaServicio.cerrarSubasta(99999L));
    }

    @Test
    public void cerrarSubastaSinUnidadesTest() throws NegocioException {
        Producto p = productoRepo.findById(5L).get();
        p.setUnidades(0);
        productoRepo.save(p);
        vencer(1L);
        subastaServicio.cerrarSubasta(1L);
        sincronizar();
        Subasta cerrada = subastaRepo.findById(1L).get();
        assertTrue(cerrada.isCerrada());
        assertNull(cerrada.getCompra());
    }

    @Test
    public void listarActivasExcluyeCerradasTest() {
        assertEquals(2, subastaServicio.listarActivas().size());
        Subasta s = subastaRepo.findById(1L).get();
        s.setCerrada(true);
        subastaRepo.save(s);
        sincronizar();
        assertEquals(1, subastaServicio.listarActivas().size());
    }

    @Test
    public void listarVencidasSinCerrarTest() {
        List<Subasta> vencidas = subastaServicio.listarVencidasSinCerrar();
        assertEquals(1, vencidas.size());
        assertEquals(3L, vencidas.get(0).getId());
    }

    // ===== tarea programada =====

    @Test
    public void tareaCierraVencidasTest() {
        vencer(1L);
        assertEquals(2, tarea.cerrarSubastasVencidas());
        sincronizar();
        assertTrue(subastaRepo.findById(1L).get().isCerrada());
        assertNotNull(subastaRepo.findById(1L).get().getCompra());
        assertTrue(subastaRepo.findById(3L).get().isCerrada());
        assertTrue(subastaServicio.listarVencidasSinCerrar().isEmpty());
        assertEquals(0, tarea.cerrarSubastasVencidas());
    }
}
