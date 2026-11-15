package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.HistorialPrecio;
import co.edu.uniquindio.proyecto.repositorios.HistorialPrecioRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class HistorialPrecioTest extends CrudTestBase {

    @Autowired
    private HistorialPrecioRepo historialPrecioRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Test
    public void registrarHistorialPrecioTest() {
        HistorialPrecio h = new HistorialPrecio();
        h.setPrecioAnterior(150000.0);
        h.setPrecioNuevo(135000.0);
        h.setDescuentoAnterior(0.0);
        h.setDescuentoNuevo(0.0);
        h.setFecha(new Date());
        h.setProducto(productoRepo.findById(4L).orElseThrow());

        HistorialPrecio guardado = historialPrecioRepo.saveAndFlush(h);
        sincronizar();

        HistorialPrecio buscado = historialPrecioRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals(150000.0, buscado.getPrecioAnterior());
        Assertions.assertEquals(135000.0, buscado.getPrecioNuevo());
        Assertions.assertEquals("Licuadora", buscado.getProducto().getNombre());
    }

    @Test
    public void eliminarHistorialPrecioTest() {
        historialPrecioRepo.deleteById(1L);
        sincronizar();

        Assertions.assertTrue(historialPrecioRepo.findById(1L).isEmpty());
        Assertions.assertEquals(2, historialPrecioRepo.count());
    }

    @Test
    public void actualizarHistorialPrecioTest() {
        HistorialPrecio h = historialPrecioRepo.findById(2L).orElseThrow();
        h.setDescuentoNuevo(0.15);
        historialPrecioRepo.saveAndFlush(h);
        sincronizar();

        Assertions.assertEquals(0.15, historialPrecioRepo.findById(2L).orElseThrow().getDescuentoNuevo());
    }

    @Test
    public void listarHistorialPrecioTest() {
        List<HistorialPrecio> lista = historialPrecioRepo.findAll();
        Assertions.assertEquals(3, lista.size());
        Assertions.assertEquals(1, productoRepo.findById(1L).orElseThrow().getMisHistorialPrecios().size());
    }
}
