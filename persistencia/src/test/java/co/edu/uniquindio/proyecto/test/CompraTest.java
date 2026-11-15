package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import co.edu.uniquindio.proyecto.repositorios.CuponRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class CompraTest extends CrudTestBase {

    @Autowired
    private CompraRepo compraRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private CuponRepo cuponRepo;

    private Compra nuevaCompra() {
        Compra c = new Compra();
        c.setFechaCompra(new Date());
        c.setMedioPago(MedioPago.PAYPAL);
        c.setUsuario(usuarioRepo.findById(8L).orElseThrow());
        c.setCupon(cuponRepo.findById(2L).orElseThrow());
        return c;
    }

    @Test
    public void registrarCompraTest() {
        Compra guardada = compraRepo.saveAndFlush(nuevaCompra());
        sincronizar();

        Compra buscada = compraRepo.findById(guardada.getId()).orElseThrow();
        Assertions.assertEquals(MedioPago.PAYPAL, buscada.getMedioPago());
        Assertions.assertEquals("Pedro Martinez", buscada.getUsuario().getNombre());
        Assertions.assertEquals("NAVIDAD20", buscada.getCupon().getCodigo());
    }

    @Test
    public void eliminarCompraTest() {
        // Las compras del SQL tienen detalles, así que se elimina una nueva
        Compra guardada = compraRepo.saveAndFlush(nuevaCompra());
        compraRepo.delete(guardada);
        sincronizar();

        Assertions.assertTrue(compraRepo.findById(guardada.getId()).isEmpty());
        Assertions.assertEquals(4, compraRepo.count());
    }

    @Test
    public void actualizarCompraTest() {
        Compra c = compraRepo.findById(2L).orElseThrow();
        c.setMedioPago(MedioPago.TARJETA);
        c.setCupon(cuponRepo.findById(1L).orElseThrow());
        compraRepo.saveAndFlush(c);
        sincronizar();

        Compra buscada = compraRepo.findById(2L).orElseThrow();
        Assertions.assertEquals(MedioPago.TARJETA, buscada.getMedioPago());
        Assertions.assertEquals("BIENVENIDA10", buscada.getCupon().getCodigo());
    }

    @Test
    public void listarCompraTest() {
        List<Compra> lista = compraRepo.findAll();
        Assertions.assertEquals(4, lista.size());
        Assertions.assertEquals(2, compraRepo.findById(1L).orElseThrow().getMisDetalleCompras().size());
    }
}
