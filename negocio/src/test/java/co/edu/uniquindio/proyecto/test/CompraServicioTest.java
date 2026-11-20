package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.ItemCompraDTO;
import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CuponRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.servicios.CompraServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import co.edu.uniquindio.proyecto.servicios.CuponServicio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CompraServicioTest extends BaseServicioTest {

    @Autowired
    private CompraServicio compraServicio;

    @Autowired
    private CuponServicio cuponServicio;

    @Autowired
    private CuponRepo cuponRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Autowired
    private SubastaServicio subastaServicio;

    @Autowired
    private SubastaRepo subastaRepo;

    /** En los datos de prueba el producto 5 está en la subasta 1 (activa): se da por terminada para poder comprarlo. */
    private void terminarSubastaDelProducto5() {
        subastaRepo.findById(1L).ifPresent(s -> {
            s.setFechaLimite(new Date(System.currentTimeMillis() - 60_000));
            subastaRepo.save(s);
        });
    }

    private int unidades(Long idProducto) {
        return productoRepo.findById(idProducto).orElseThrow(AssertionError::new).getUnidades();
    }

    private List<DetalleCompra> detallesOrdenados(Long idCompra) throws NegocioException {
        List<DetalleCompra> detalles = compraServicio.listarDetalles(idCompra);
        detalles.sort((a, b) -> a.getProducto().getId().compareTo(b.getProducto().getId()));
        return detalles;
    }

    private void assertMensaje(String esperado, NegocioException e) {
        Assertions.assertEquals(esperado, e.getMessage());
    }

    @Test
    public void comprarFelizTest() throws NegocioException {
        Compra compra = compraServicio.comprar(7L,
                Arrays.asList(new ItemCompraDTO(1L, 2), new ItemCompraDTO(4L, 1)), MedioPago.PSE, null);
        sincronizar();

        Compra guardada = compraServicio.obtenerCompra(compra.getId());
        Assertions.assertNotNull(guardada.getFechaCompra());
        Assertions.assertEquals(MedioPago.PSE, guardada.getMedioPago());
        Assertions.assertNull(guardada.getCupon());

        List<DetalleCompra> detalles = detallesOrdenados(compra.getId());
        Assertions.assertEquals(2, detalles.size());
        Assertions.assertEquals(21000.0, detalles.get(0).getPrecioProducto());
        Assertions.assertEquals(2, detalles.get(0).getUnidades());
        Assertions.assertEquals(150000.0, detalles.get(1).getPrecioProducto());
        Assertions.assertEquals(18, unidades(1L));
        Assertions.assertEquals(7, unidades(4L));
        Assertions.assertEquals(192000.0, compraServicio.calcularTotal(compra.getId()));
    }

    @Test
    public void comprarConCuponTest() throws NegocioException {
        Compra compra = compraServicio.comprar(7L,
                Arrays.asList(new ItemCompraDTO(1L, 2), new ItemCompraDTO(4L, 1)), MedioPago.TARJETA, "NAVIDAD20");
        sincronizar();

        List<DetalleCompra> detalles = detallesOrdenados(compra.getId());
        Assertions.assertEquals(16800.0, detalles.get(0).getPrecioProducto());
        Assertions.assertEquals(120000.0, detalles.get(1).getPrecioProducto());
        Assertions.assertEquals(153600.0, compraServicio.calcularTotal(compra.getId()));
        Cupon cupon = cuponServicio.obtenerPorCodigo("NAVIDAD20");
        Assertions.assertEquals(1, cupon.getUsosRealizados());
        Assertions.assertEquals(cupon.getId(), compraServicio.obtenerCompra(compra.getId()).getCupon().getId());
    }

    @Test
    public void comprarCuponVencidoTest() throws NegocioException {
        int antes = compraServicio.listarComprasUsuario(7L).size();
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, 1)),
                        MedioPago.PSE, "VERANO15"));
        assertMensaje("El cupón VERANO15 está vencido", e);
        sincronizar();
        Assertions.assertEquals(antes, compraServicio.listarComprasUsuario(7L).size());
        Assertions.assertEquals(20, unidades(1L));
    }

    @Test
    public void comprarCuponSinUsosTest() throws NegocioException {
        Cupon agotado = new Cupon();
        agotado.setCodigo("AGOTADO");
        agotado.setPorcentaje(0.5);
        agotado.setFechaVencimiento(new Date(System.currentTimeMillis() + 86_400_000L));
        agotado.setUsosMaximos(1);
        agotado.setUsosRealizados(1);
        cuponRepo.save(agotado);
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, 1)),
                        MedioPago.PSE, "AGOTADO"));
        assertMensaje("El cupón AGOTADO ya no tiene usos disponibles", e);
    }

    @Test
    public void comprarCuponInexistenteTest() {
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, 1)),
                        MedioPago.PSE, "NOEXISTE"));
        assertMensaje("El cupón NOEXISTE no existe", e);
    }

    @Test
    public void comprarCuponEnBlancoEsSinCuponTest() throws NegocioException {
        Compra compra = compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, 1)),
                MedioPago.PSE, "  ");
        Assertions.assertNull(compra.getCupon());
    }

    @Test
    public void comprarMasUnidadesQueLasDisponiblesTest() throws NegocioException {
        terminarSubastaDelProducto5();
        int comprasAntes = compraServicio.listarComprasUsuario(7L).size();
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L,
                        Arrays.asList(new ItemCompraDTO(1L, 2), new ItemCompraDTO(5L, 3)), MedioPago.PSE, null));
        Assertions.assertTrue(e.getMessage().endsWith("(disponibles: 2)"));
        sincronizar();
        Assertions.assertEquals(20, unidades(1L));
        Assertions.assertEquals(2, unidades(5L));
        Assertions.assertEquals(comprasAntes, compraServicio.listarComprasUsuario(7L).size());
    }

    @Test
    public void comprarProductoVencidoTest() {
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(6L, 1)),
                        MedioPago.PSE, null));
        Assertions.assertTrue(e.getMessage().endsWith("no está disponible"));
    }

    @Test
    public void comprarProductoEnSubastaTest() throws NegocioException {
        Producto producto = productoRepo.findById(1L).orElseThrow(AssertionError::new);
        subastaServicio.crearSubasta(1L, producto.getUsuario().getId(), 1);
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, 1)),
                        MedioPago.PSE, null));
        Assertions.assertTrue(e.getMessage().endsWith("está en subasta"));
        Assertions.assertEquals(20, unidades(1L));
    }

    @Test
    public void comprarProductoPropioTest() {
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(3L, 1)),
                        MedioPago.PSE, null));
        assertMensaje("No puede comprar su propio producto", e);
    }

    @Test
    public void comprarProductoInexistenteTest() {
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(999L, 1)),
                        MedioPago.PSE, null));
        assertMensaje("El producto no existe", e);
    }

    @Test
    public void comprarUnidadesInvalidasTest() {
        for (int unidades : new int[]{0, -1}) {
            NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                    compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, unidades)),
                            MedioPago.PSE, null));
            assertMensaje("Las unidades deben ser mayores que cero", e);
        }
    }

    @Test
    public void comprarListaVaciaOnulaTest() {
        NegocioException e1 = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.emptyList(), MedioPago.PSE, null));
        assertMensaje("La compra debe tener al menos un producto", e1);
        NegocioException e2 = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, null, MedioPago.PSE, null));
        assertMensaje("La compra debe tener al menos un producto", e2);
    }

    @Test
    public void comprarSinMedioPagoTest() {
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(1L, 1)), null, null));
        assertMensaje("El medio de pago es obligatorio", e);
    }

    @Test
    public void comprarUsuarioInexistenteTest() {
        NegocioException e = Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(999L, Collections.singletonList(new ItemCompraDTO(1L, 1)),
                        MedioPago.PSE, null));
        assertMensaje("El usuario no existe", e);
        Assertions.assertThrows(NegocioException.class, () ->
                compraServicio.comprar(null, Collections.singletonList(new ItemCompraDTO(1L, 1)),
                        MedioPago.PSE, null));
    }

    @Test
    public void comprarMismoProductoRepetidoSeAgrupaTest() throws NegocioException {
        Compra compra = compraServicio.comprar(7L,
                Arrays.asList(new ItemCompraDTO(1L, 2), new ItemCompraDTO(1L, 3)), MedioPago.PAYPAL, null);
        sincronizar();
        List<DetalleCompra> detalles = compraServicio.listarDetalles(compra.getId());
        Assertions.assertEquals(1, detalles.size());
        Assertions.assertEquals(5, detalles.get(0).getUnidades());
        Assertions.assertEquals(15, unidades(1L));
    }

    @Test
    public void comprarTodasLasUnidadesTest() throws NegocioException {
        terminarSubastaDelProducto5();
        compraServicio.comprar(7L, Collections.singletonList(new ItemCompraDTO(5L, 2)), MedioPago.PSE, null);
        sincronizar();
        Assertions.assertEquals(0, unidades(5L));
    }

    @Test
    public void listarComprasUsuarioTest() throws NegocioException {
        List<Compra> compras = compraServicio.listarComprasUsuario(6L);
        Assertions.assertEquals(1, compras.size());
        Assertions.assertEquals(1L, compras.get(0).getId());
        Assertions.assertTrue(compraServicio.listarComprasUsuario(8L).isEmpty());
        Assertions.assertThrows(NegocioException.class, () -> compraServicio.listarComprasUsuario(999L));
    }

    @Test
    public void listarDetallesYTotalTest() throws NegocioException {
        Assertions.assertEquals(2, compraServicio.listarDetalles(1L).size());
        Assertions.assertEquals(45000.0, compraServicio.calcularTotal(1L));
        Assertions.assertThrows(NegocioException.class, () -> compraServicio.listarDetalles(999L));
        Assertions.assertThrows(NegocioException.class, () -> compraServicio.calcularTotal(999L));
    }
}
