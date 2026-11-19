package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.entidades.MotivoReporte;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.ReporteProductoServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ReporteProductoServicioTest extends BaseServicioTest {

    private static final String ADMIN_EMAIL = "laroca@uniquindio.com";

    @Autowired
    private ReporteProductoServicio reporteServicio;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private ProductoRepo productoRepo;

    private ReporteProducto nuevoReporte(Long idUsuario, Long idProducto) {
        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        Producto producto = new Producto();
        producto.setId(idProducto);
        ReporteProducto r = new ReporteProducto();
        r.setMotivo(MotivoReporte.FRAUDE);
        r.setDescripcion("Descripción del reporte");
        r.setUsuario(usuario);
        r.setProducto(producto);
        return r;
    }

    private ReporteProducto reporteCompleto(Long idUsuario, Long idProducto) {
        ReporteProducto r = nuevoReporte(idUsuario, idProducto);
        r.setUsuario(usuarioRepo.findById(idUsuario).orElseThrow(AssertionError::new));
        r.setProducto(productoRepo.findById(idProducto).orElseThrow(AssertionError::new));
        r.setFecha(new Date());
        r.setEstado(EstadoReporte.PENDIENTE);
        return r;
    }

    // ---------- CRUD ----------

    @Test
    public void registrarReporteTest() throws Exception {
        ReporteProducto guardado = reporteServicio.registrarReporte(reporteCompleto(6L, 1L));
        sincronizar();
        assertNotNull(guardado.getId());
        ReporteProducto leido = reporteServicio.obtenerReporte(guardado.getId());
        assertEquals(MotivoReporte.FRAUDE, leido.getMotivo());
        assertEquals("Descripción del reporte", leido.getDescripcion());
    }

    @Test
    public void registrarConIdExistenteTest() {
        ReporteProducto r = reporteCompleto(6L, 1L);
        r.setId(1L);
        NegocioException e = assertThrows(NegocioException.class, () -> reporteServicio.registrarReporte(r));
        assertEquals("El reporte ya existe", e.getMessage());
    }

    @Test
    public void actualizarReporteTest() throws Exception {
        ReporteProducto r = reporteServicio.obtenerReporte(1L);
        r.setDescripcion("Descripción modificada");
        reporteServicio.actualizarReporte(r);
        sincronizar();
        assertEquals("Descripción modificada", reporteServicio.obtenerReporte(1L).getDescripcion());
    }

    @Test
    public void actualizarConIdNuloTest() {
        assertThrows(NegocioException.class, () -> reporteServicio.actualizarReporte(nuevoReporte(6L, 1L)));
    }

    @Test
    public void actualizarInexistenteTest() {
        ReporteProducto r = nuevoReporte(6L, 1L);
        r.setId(99999L);
        assertThrows(NegocioException.class, () -> reporteServicio.actualizarReporte(r));
    }

    @Test
    public void eliminarReporteTest() throws Exception {
        reporteServicio.eliminarReporte(1L);
        sincronizar();
        assertThrows(NegocioException.class, () -> reporteServicio.obtenerReporte(1L));
    }

    @Test
    public void eliminarInexistenteTest() {
        assertThrows(NegocioException.class, () -> reporteServicio.eliminarReporte(99999L));
    }

    @Test
    public void eliminarNuloTest() {
        assertThrows(NegocioException.class, () -> reporteServicio.eliminarReporte(null));
    }

    @Test
    public void listarReportesTest() {
        assertEquals(3, reporteServicio.listarReportes().size());
    }

    @Test
    public void obtenerInexistenteTest() {
        assertThrows(NegocioException.class, () -> reporteServicio.obtenerReporte(99999L));
    }

    @Test
    public void obtenerNuloTest() {
        assertThrows(NegocioException.class, () -> reporteServicio.obtenerReporte(null));
    }

    // ---------- reportarProducto ----------

    @Test
    public void reportarProductoTest() throws Exception {
        ReporteProducto r = nuevoReporte(6L, 1L);
        // Lo que venga en fecha y estado se ignora.
        r.setEstado(EstadoReporte.ACEPTADO);
        r.setFecha(new Date(0L));
        ReporteProducto guardado = reporteServicio.reportarProducto(r);
        sincronizar();
        ReporteProducto leido = reporteServicio.obtenerReporte(guardado.getId());
        assertEquals(EstadoReporte.PENDIENTE, leido.getEstado());
        assertNotNull(leido.getFecha());
        assertTrue(leido.getFecha().after(new Date(0L)));
        assertEquals(6L, leido.getUsuario().getId());
        assertEquals(1L, leido.getProducto().getId());
    }

    @Test
    public void yaReportoTest() throws Exception {
        // El usuario 9 ya reportó el producto 3 en los datos de prueba.
        assertTrue(reporteServicio.yaReporto(9L, 3L));
        assertFalse(reporteServicio.yaReporto(6L, 1L));
        reporteServicio.reportarProducto(nuevoReporte(6L, 1L));
        sincronizar();
        assertTrue(reporteServicio.yaReporto(6L, 1L));
    }

    @Test
    public void yaReportoConIdsNulosTest() {
        assertFalse(reporteServicio.yaReporto(null, 1L));
        assertFalse(reporteServicio.yaReporto(6L, null));
    }

    @Test
    public void reportarPropioProductoTest() {
        // El producto 1 es del usuario 5.
        NegocioException e = assertThrows(NegocioException.class,
                () -> reporteServicio.reportarProducto(nuevoReporte(5L, 1L)));
        assertEquals("No puede reportar su propio producto", e.getMessage());
    }

    @Test
    public void reportarDosVecesTest() {
        // El usuario 9 ya reportó el producto 3 en los datos de prueba.
        NegocioException e = assertThrows(NegocioException.class,
                () -> reporteServicio.reportarProducto(nuevoReporte(9L, 3L)));
        assertEquals("Ya reportó este producto", e.getMessage());
    }

    @Test
    public void reportarConUsuarioInexistenteTest() {
        assertThrows(NegocioException.class, () -> reporteServicio.reportarProducto(nuevoReporte(99999L, 1L)));
    }

    @Test
    public void reportarConProductoInexistenteTest() {
        assertThrows(NegocioException.class, () -> reporteServicio.reportarProducto(nuevoReporte(6L, 99999L)));
    }

    @Test
    public void reportarSinUsuarioTest() {
        ReporteProducto r = nuevoReporte(6L, 1L);
        r.setUsuario(null);
        assertThrows(NegocioException.class, () -> reporteServicio.reportarProducto(r));
    }

    @Test
    public void reportarSinProductoTest() {
        ReporteProducto r = nuevoReporte(6L, 1L);
        r.setProducto(null);
        assertThrows(NegocioException.class, () -> reporteServicio.reportarProducto(r));
    }

    // ---------- listados ----------

    @Test
    public void listarPendientesTest() {
        assertEquals(1, reporteServicio.listarPendientes().size());
        assertEquals(EstadoReporte.PENDIENTE, reporteServicio.listarPendientes().get(0).getEstado());
    }

    @Test
    public void listarPorProductoTest() {
        assertEquals(1, reporteServicio.listarPorProducto(3L).size());
        assertEquals(0, reporteServicio.listarPorProducto(4L).size());
    }

    // ---------- cambiarEstado ----------

    @Test
    public void cambiarEstadoTest() throws Exception {
        reporteServicio.cambiarEstado(1L, EstadoReporte.ACEPTADO, "Se confirmó el fraude", ADMIN_EMAIL);
        sincronizar();
        ReporteProducto leido = reporteServicio.obtenerReporte(1L);
        assertEquals(EstadoReporte.ACEPTADO, leido.getEstado());
        assertEquals("Se confirmó el fraude", leido.getRespuestaAdministrador());
        assertEquals(ADMIN_EMAIL, leido.getAdministrador().getEmail());
    }

    @Test
    public void cambiarEstadoAEnRevisionSinRespuestaTest() throws Exception {
        reporteServicio.cambiarEstado(1L, EstadoReporte.EN_REVISION, null, ADMIN_EMAIL);
        sincronizar();
        ReporteProducto leido = reporteServicio.obtenerReporte(1L);
        assertEquals(EstadoReporte.EN_REVISION, leido.getEstado());
        assertEquals(ADMIN_EMAIL, leido.getAdministrador().getEmail());
    }

    @Test
    public void cambiarEstadoDesdeEnRevisionTest() throws Exception {
        reporteServicio.cambiarEstado(2L, EstadoReporte.RECHAZADO, "Las fotos son correctas", ADMIN_EMAIL);
        sincronizar();
        assertEquals(EstadoReporte.RECHAZADO, reporteServicio.obtenerReporte(2L).getEstado());
    }

    @Test
    public void cambiarEstadoAPendienteTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> reporteServicio.cambiarEstado(2L, EstadoReporte.PENDIENTE, "x", ADMIN_EMAIL));
        assertEquals("No se puede volver a PENDIENTE", e.getMessage());
    }

    @Test
    public void cambiarEstadoSinRespuestaTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> reporteServicio.cambiarEstado(1L, EstadoReporte.ACEPTADO, null, ADMIN_EMAIL));
        assertEquals("La respuesta es obligatoria", e.getMessage());
    }

    @Test
    public void cambiarEstadoConRespuestaVaciaTest() {
        assertThrows(NegocioException.class,
                () -> reporteServicio.cambiarEstado(1L, EstadoReporte.RECHAZADO, "   ", ADMIN_EMAIL));
    }

    @Test
    public void cambiarEstadoAdministradorInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> reporteServicio.cambiarEstado(1L, EstadoReporte.ACEPTADO, "Ok", "noexiste@uniquindio.com"));
        assertEquals("El administrador no existe", e.getMessage());
    }

    @Test
    public void cambiarEstadoReporteInexistenteTest() {
        assertThrows(NegocioException.class,
                () -> reporteServicio.cambiarEstado(99999L, EstadoReporte.ACEPTADO, "Ok", ADMIN_EMAIL));
    }

    @Test
    public void cambiarEstadoReporteYaResueltoTest() {
        // El reporte 3 está RECHAZADO en los datos de prueba.
        NegocioException e = assertThrows(NegocioException.class,
                () -> reporteServicio.cambiarEstado(3L, EstadoReporte.ACEPTADO, "Ok", ADMIN_EMAIL));
        assertEquals("El reporte ya fue resuelto", e.getMessage());
    }
}
