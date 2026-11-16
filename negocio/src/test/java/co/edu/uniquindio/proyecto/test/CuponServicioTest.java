package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CuponServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class CuponServicioTest extends BaseServicioTest {

    private static final long UN_DIA = 24L * 60 * 60 * 1000;

    @Autowired
    private CuponServicio cuponServicio;

    private Cupon nuevoCupon(String codigo, int usosMaximos, int usosRealizados) {
        Cupon c = new Cupon();
        c.setCodigo(codigo);
        c.setPorcentaje(0.25);
        c.setFechaVencimiento(new Date(System.currentTimeMillis() + 30 * UN_DIA));
        c.setUsosMaximos(usosMaximos);
        c.setUsosRealizados(usosRealizados);
        return c;
    }

    // ---------- CRUD ----------

    @Test
    public void registrarCuponTest() throws Exception {
        Cupon guardado = cuponServicio.registrarCupon(nuevoCupon("PROMO25", 10, 0));
        sincronizar();
        assertNotNull(guardado.getId());
        Cupon leido = cuponServicio.obtenerCupon(guardado.getId());
        assertEquals("PROMO25", leido.getCodigo());
        assertEquals(10, leido.getUsosMaximos());
    }

    @Test
    public void registrarCodigoRepetidoTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> cuponServicio.registrarCupon(nuevoCupon("NAVIDAD20", 10, 0)));
        assertEquals("El cupón NAVIDAD20 ya existe", e.getMessage());
    }

    @Test
    public void registrarConIdExistenteTest() {
        Cupon c = nuevoCupon("PROMO25", 10, 0);
        c.setId(1L);
        NegocioException e = assertThrows(NegocioException.class, () -> cuponServicio.registrarCupon(c));
        assertEquals("El cupón ya existe", e.getMessage());
    }

    @Test
    public void actualizarCuponTest() throws Exception {
        Cupon modificado = nuevoCupon("NAVIDAD25", 80, 0);
        modificado.setId(2L);
        cuponServicio.actualizarCupon(modificado);
        sincronizar();
        Cupon leido = cuponServicio.obtenerCupon(2L);
        assertEquals("NAVIDAD25", leido.getCodigo());
        assertEquals(80, leido.getUsosMaximos());
    }

    @Test
    public void actualizarConMismoCodigoTest() throws Exception {
        Cupon modificado = nuevoCupon("NAVIDAD20", 77, 0);
        modificado.setId(2L);
        cuponServicio.actualizarCupon(modificado);
        sincronizar();
        assertEquals(77, cuponServicio.obtenerCupon(2L).getUsosMaximos());
    }

    @Test
    public void actualizarConCodigoDeOtroTest() {
        // Objeto nuevo (no el administrado por Hibernate) para evitar el flush automático.
        Cupon c = nuevoCupon("BIENVENIDA10", 50, 0);
        c.setId(2L);
        NegocioException e = assertThrows(NegocioException.class, () -> cuponServicio.actualizarCupon(c));
        assertEquals("El cupón BIENVENIDA10 ya existe", e.getMessage());
    }

    @Test
    public void actualizarConIdNuloTest() {
        assertThrows(NegocioException.class, () -> cuponServicio.actualizarCupon(nuevoCupon("OTRO", 5, 0)));
    }

    @Test
    public void actualizarInexistenteTest() {
        Cupon c = nuevoCupon("OTRO", 5, 0);
        c.setId(99999L);
        assertThrows(NegocioException.class, () -> cuponServicio.actualizarCupon(c));
    }

    @Test
    public void eliminarCuponTest() throws Exception {
        // El cupón 2 no se ha usado en ninguna compra.
        cuponServicio.eliminarCupon(2L);
        sincronizar();
        assertThrows(NegocioException.class, () -> cuponServicio.obtenerCupon(2L));
    }

    @Test
    public void eliminarCuponUsadoTest() {
        // El cupón 1 tiene usos realizados y compras asociadas.
        NegocioException e = assertThrows(NegocioException.class, () -> cuponServicio.eliminarCupon(1L));
        assertEquals("El cupón ya se usó en compras y no se puede eliminar", e.getMessage());
    }

    @Test
    public void eliminarCuponConComprasSinUsosRegistradosTest() throws Exception {
        // El cupón 1 se usó en las compras 1 y 4, aunque su contador esté en cero.
        Cupon c = nuevoCupon("BIENVENIDA10", 100, 0);
        c.setId(1L);
        cuponServicio.actualizarCupon(c);
        sincronizar();
        NegocioException e = assertThrows(NegocioException.class, () -> cuponServicio.eliminarCupon(1L));
        assertEquals("El cupón ya se usó en compras y no se puede eliminar", e.getMessage());
    }

    @Test
    public void eliminarInexistenteTest() {
        assertThrows(NegocioException.class, () -> cuponServicio.eliminarCupon(99999L));
    }

    @Test
    public void eliminarNuloTest() {
        assertThrows(NegocioException.class, () -> cuponServicio.eliminarCupon(null));
    }

    @Test
    public void listarCuponesTest() {
        assertEquals(3, cuponServicio.listarCupones().size());
    }

    @Test
    public void obtenerInexistenteTest() {
        assertThrows(NegocioException.class, () -> cuponServicio.obtenerCupon(99999L));
    }

    @Test
    public void obtenerNuloTest() {
        assertThrows(NegocioException.class, () -> cuponServicio.obtenerCupon(null));
    }

    // ---------- por código ----------

    @Test
    public void obtenerPorCodigoTest() throws Exception {
        assertEquals(2L, cuponServicio.obtenerPorCodigo("NAVIDAD20").getId());
    }

    @Test
    public void obtenerPorCodigoInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> cuponServicio.obtenerPorCodigo("NOEXISTE"));
        assertEquals("El cupón NOEXISTE no existe", e.getMessage());
    }

    @Test
    public void obtenerDisponibleTest() throws Exception {
        assertEquals(1L, cuponServicio.obtenerDisponible("BIENVENIDA10").getId());
    }

    @Test
    public void obtenerDisponibleInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> cuponServicio.obtenerDisponible("NOEXISTE"));
        assertEquals("El cupón NOEXISTE no existe", e.getMessage());
    }

    @Test
    public void obtenerDisponibleVencidoTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> cuponServicio.obtenerDisponible("VERANO15"));
        assertEquals("El cupón VERANO15 está vencido", e.getMessage());
    }

    @Test
    public void obtenerDisponibleSinUsosTest() throws Exception {
        cuponServicio.registrarCupon(nuevoCupon("AGOTADO", 5, 5));
        sincronizar();
        NegocioException e = assertThrows(NegocioException.class, () -> cuponServicio.obtenerDisponible("AGOTADO"));
        assertEquals("El cupón AGOTADO ya no tiene usos disponibles", e.getMessage());
    }
}
