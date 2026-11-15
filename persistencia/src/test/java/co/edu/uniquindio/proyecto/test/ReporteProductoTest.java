package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.entidades.MotivoReporte;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.ReporteProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class ReporteProductoTest extends CrudTestBase {

    @Autowired
    private ReporteProductoRepo reporteProductoRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Autowired
    private AdministradorRepo administradorRepo;

    @Test
    public void registrarReporteProductoTest() {
        ReporteProducto r = new ReporteProducto();
        r.setMotivo(MotivoReporte.PRODUCTO_PROHIBIDO);
        r.setDescripcion("Venden medicamentos sin formula");
        r.setFecha(new Date());
        r.setUsuario(usuarioRepo.findById(8L).orElseThrow());
        r.setProducto(productoRepo.findById(4L).orElseThrow());

        ReporteProducto guardado = reporteProductoRepo.saveAndFlush(r);
        sincronizar();

        ReporteProducto buscado = reporteProductoRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals(MotivoReporte.PRODUCTO_PROHIBIDO, buscado.getMotivo());
        Assertions.assertEquals(EstadoReporte.PENDIENTE, buscado.getEstado());
        Assertions.assertNull(buscado.getAdministrador());
    }

    @Test
    public void eliminarReporteProductoTest() {
        reporteProductoRepo.deleteById(1L);
        sincronizar();

        Assertions.assertTrue(reporteProductoRepo.findById(1L).isEmpty());
        Assertions.assertEquals(2, reporteProductoRepo.count());
    }

    @Test
    public void actualizarReporteProductoTest() {
        // El administrador 3 acepta el reporte pendiente 1
        ReporteProducto r = reporteProductoRepo.findById(1L).orElseThrow();
        r.setEstado(EstadoReporte.ACEPTADO);
        r.setRespuestaAdministrador("Se confirmo el fraude, el producto se retira");
        r.setAdministrador(administradorRepo.findById(3L).orElseThrow());
        reporteProductoRepo.saveAndFlush(r);
        sincronizar();

        ReporteProducto buscado = reporteProductoRepo.findById(1L).orElseThrow();
        Assertions.assertEquals(EstadoReporte.ACEPTADO, buscado.getEstado());
        Assertions.assertEquals("La Roca", buscado.getAdministrador().getNombre());
        Assertions.assertTrue(reporteProductoRepo.listarPorEstado(EstadoReporte.PENDIENTE).isEmpty());
    }

    @Test
    public void listarReporteProductoTest() {
        List<ReporteProducto> lista = reporteProductoRepo.findAll();
        Assertions.assertEquals(3, lista.size());
    }
}
