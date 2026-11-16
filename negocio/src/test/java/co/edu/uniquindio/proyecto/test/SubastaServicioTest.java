package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class SubastaServicioTest extends BaseServicioTest {

    @Autowired
    private SubastaServicio subastaServicio;

    @Autowired
    private ProductoRepo productoRepo;

    private Date enElFuturo() {
        Calendar cal = Calendar.getInstance();
        cal.set(2031, Calendar.JANUARY, 1, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /** El producto 1 no tiene subasta en el SQL. */
    private Subasta nuevaSubasta() {
        Subasta s = new Subasta();
        s.setFechaLimite(enElFuturo());
        s.setProducto(productoRepo.findById(1L).orElseThrow());
        return s;
    }

    @Test
    public void registrarSubastaTest() throws Exception {
        Subasta guardada = subastaServicio.registrarSubasta(nuevaSubasta());
        sincronizar();
        assertNotNull(guardada.getId());
        Subasta leida = subastaServicio.obtenerSubasta(guardada.getId());
        assertEquals(1L, leida.getProducto().getId());
        assertEquals(enElFuturo().getTime(), leida.getFechaLimite().getTime());
    }

    @Test
    public void registrarConIdExistenteTest() {
        Subasta s = nuevaSubasta();
        s.setId(1L);
        assertThrows(NegocioException.class, () -> subastaServicio.registrarSubasta(s));
    }

    @Test
    public void actualizarSubastaTest() throws Exception {
        Subasta s = subastaServicio.obtenerSubasta(1L);
        s.setFechaLimite(enElFuturo());
        subastaServicio.actualizarSubasta(s);
        sincronizar();
        assertEquals(enElFuturo().getTime(), subastaServicio.obtenerSubasta(1L).getFechaLimite().getTime());
    }

    @Test
    public void actualizarConIdNuloTest() {
        assertThrows(NegocioException.class, () -> subastaServicio.actualizarSubasta(nuevaSubasta()));
    }

    @Test
    public void actualizarInexistenteTest() {
        Subasta s = nuevaSubasta();
        s.setId(99999L);
        assertThrows(NegocioException.class, () -> subastaServicio.actualizarSubasta(s));
    }

    @Test
    public void eliminarSubastaSinPujasTest() throws Exception {
        Subasta guardada = subastaServicio.registrarSubasta(nuevaSubasta());
        sincronizar();
        Long id = guardada.getId();
        subastaServicio.eliminarSubasta(id);
        sincronizar();
        assertThrows(NegocioException.class, () -> subastaServicio.obtenerSubasta(id));
    }

    @Test
    public void eliminarSubastaConPujasTest() {
        assertThrows(NegocioException.class, () -> subastaServicio.eliminarSubasta(1L));
    }

    @Test
    public void eliminarInexistenteTest() {
        assertThrows(NegocioException.class, () -> subastaServicio.eliminarSubasta(99999L));
    }

    @Test
    public void listarSubastasTest() {
        assertEquals(3, subastaServicio.listarSubastas().size());
    }

    @Test
    public void listarActivasTest() {
        assertEquals(2, subastaServicio.listarActivas().size());
    }

    @Test
    public void obtenerInexistenteTest() {
        assertThrows(NegocioException.class, () -> subastaServicio.obtenerSubasta(99999L));
    }

    @Test
    public void obtenerNuloTest() {
        assertThrows(NegocioException.class, () -> subastaServicio.obtenerSubasta(null));
    }
}
