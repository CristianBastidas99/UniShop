package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import co.edu.uniquindio.proyecto.repositorios.DetalleCompraRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DetalleCompraTest extends CrudTestBase {

    @Autowired
    private DetalleCompraRepo detalleCompraRepo;

    @Autowired
    private CompraRepo compraRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Test
    public void registrarDetalleCompraTest() {
        DetalleCompra d = new DetalleCompra();
        d.setUnidades(3);
        d.setPrecioProducto(3000.0);
        d.setCompra(compraRepo.findById(3L).orElseThrow());
        d.setProducto(productoRepo.findById(3L).orElseThrow());

        DetalleCompra guardado = detalleCompraRepo.saveAndFlush(d);
        sincronizar();

        DetalleCompra buscado = detalleCompraRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals(3, buscado.getUnidades());
        Assertions.assertEquals(3000.0, buscado.getPrecioProducto());
        Assertions.assertEquals(2, detalleCompraRepo.listarPorCompra(3L).size());
    }

    @Test
    public void eliminarDetalleCompraTest() {
        detalleCompraRepo.deleteById(5L);
        sincronizar();

        Assertions.assertTrue(detalleCompraRepo.findById(5L).isEmpty());
        Assertions.assertEquals(4, detalleCompraRepo.count());
    }

    @Test
    public void actualizarDetalleCompraTest() {
        DetalleCompra d = detalleCompraRepo.findById(1L).orElseThrow();
        d.setUnidades(4);
        detalleCompraRepo.saveAndFlush(d);
        sincronizar();

        Assertions.assertEquals(4, detalleCompraRepo.findById(1L).orElseThrow().getUnidades());
    }

    @Test
    public void listarDetalleCompraTest() {
        List<DetalleCompra> lista = detalleCompraRepo.findAll();
        Assertions.assertEquals(5, lista.size());
    }
}
