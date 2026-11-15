package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class SubastaTest extends CrudTestBase {

    private static final long UNA_HORA = 60L * 60 * 1000;

    @Autowired
    private SubastaRepo subastaRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Test
    public void registrarSubastaTest() {
        Date fechaLimite = new Date(System.currentTimeMillis() + 48 * UNA_HORA);
        Subasta s = new Subasta();
        s.setFechaLimite(fechaLimite);
        s.setProducto(productoRepo.findById(4L).orElseThrow());

        Subasta guardada = subastaRepo.saveAndFlush(s);
        sincronizar();

        Subasta buscada = subastaRepo.findById(guardada.getId()).orElseThrow();
        Assertions.assertEquals("Licuadora", buscada.getProducto().getNombre());
        Assertions.assertEquals(fechaLimite.getTime() / 1000, buscada.getFechaLimite().getTime() / 1000);
    }

    @Test
    public void eliminarSubastaTest() {
        // La subasta 3 (vencida) no tiene pujas
        subastaRepo.deleteById(3L);
        sincronizar();

        Assertions.assertTrue(subastaRepo.findById(3L).isEmpty());
        Assertions.assertEquals(2, subastaRepo.count());
    }

    @Test
    public void actualizarSubastaTest() {
        // Se amplía la subasta 2 un día
        Subasta s = subastaRepo.findById(2L).orElseThrow();
        Date nuevaFecha = new Date(s.getFechaLimite().getTime() + 24 * UNA_HORA);
        s.setFechaLimite(nuevaFecha);
        subastaRepo.saveAndFlush(s);
        sincronizar();

        Assertions.assertEquals(nuevaFecha.getTime(), subastaRepo.findById(2L).orElseThrow().getFechaLimite().getTime());
    }

    @Test
    public void listarSubastaTest() {
        List<Subasta> lista = subastaRepo.findAll();
        Assertions.assertEquals(3, lista.size());
        Assertions.assertEquals(2, subastaRepo.findById(1L).orElseThrow().getMisSubastaUsuarios().size());
    }
}
