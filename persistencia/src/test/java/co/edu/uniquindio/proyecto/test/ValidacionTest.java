package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pruebas de las restricciones (Bean Validation) de las entidades.
 * Se usa saveAndFlush para que el INSERT se ejecute y se disparen las validaciones.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ValidacionTest {

    private static final long UN_DIA = 24L * 60 * 60 * 1000;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Autowired
    private ComentarioRepo comentarioRepo;

    @Autowired
    private HistorialPrecioRepo historialPrecioRepo;

    @Autowired
    private ReporteProductoRepo reporteProductoRepo;

    @Autowired
    private CuponRepo cuponRepo;

    @Autowired
    private CompraRepo compraRepo;

    private Usuario vendedor;

    @BeforeEach
    public void crearVendedor() {
        Usuario u = new Usuario();
        u.setNombre("Vendedor de prueba");
        u.setEmail("vendedor.validacion@mail.com");
        u.setPassword("12345");
        vendedor = usuarioRepo.saveAndFlush(u);
    }

    private Producto productoValido() {
        Producto p = new Producto();
        p.setNombre("Cepillo Electrico");
        p.setDescripcion("Es el mejor producto que veras");
        p.setUnidades(20);
        p.setPrecio(30000.0);
        p.setDescuento(0.0);
        p.setFechaLimite(new Date(System.currentTimeMillis() + 30 * UN_DIA));
        p.setUsuario(vendedor);
        p.setMisImagenes(List.of("uploads/cepillo.png"));
        return p;
    }

    /** Devuelve los atributos que violaron alguna restricción. */
    private Set<String> atributosInvalidos(ConstraintViolationException e) {
        return e.getConstraintViolations().stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    public void productoValidoSeGuardaTest() {
        Producto guardado = productoRepo.saveAndFlush(productoValido());
        Assertions.assertNotNull(guardado.getId());
    }

    @Test
    public void productoConPrecioNegativoTest() {
        Producto p = productoValido();
        p.setPrecio(-100.0);

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> productoRepo.saveAndFlush(p));
        Assertions.assertEquals(Set.of("precio"), atributosInvalidos(e));
    }

    @Test
    public void productoSinNombreTest() {
        Producto p = productoValido();
        p.setNombre("  ");

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> productoRepo.saveAndFlush(p));
        Assertions.assertEquals(Set.of("nombre"), atributosInvalidos(e));
    }

    @Test
    public void productoSinVendedorNiImagenesTest() {
        Producto p = productoValido();
        p.setUsuario(null);
        p.setMisImagenes(List.of());

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> productoRepo.saveAndFlush(p));
        Assertions.assertEquals(Set.of("usuario", "misImagenes"), atributosInvalidos(e));
    }

    @Test
    public void productoConDescuentoMayorAUnoTest() {
        Producto p = productoValido();
        p.setDescuento(1.5);

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> productoRepo.saveAndFlush(p));
        Assertions.assertEquals(Set.of("descuento"), atributosInvalidos(e));
    }

    @Test
    public void comentarioConCalificacionSeisTest() {
        Producto producto = productoRepo.saveAndFlush(productoValido());

        Comentario c = new Comentario();
        c.setMensaje("Muy buen producto");
        c.setCalificacion(6);
        c.setFecha(new Date());
        c.setUsuario(vendedor);
        c.setProducto(producto);

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> comentarioRepo.saveAndFlush(c));
        Assertions.assertEquals(Set.of("calificacion"), atributosInvalidos(e));
    }

    @Test
    public void usuarioConEmailInvalidoTest() {
        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail("esto-no-es-un-correo");
        u.setPassword("12345");

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> usuarioRepo.saveAndFlush(u));
        Assertions.assertEquals(Set.of("email"), atributosInvalidos(e));
    }

    // ===== Necesidades extra (Paso 1.3) =====

    private HistorialPrecio historialValido(Producto producto) {
        HistorialPrecio h = new HistorialPrecio();
        h.setPrecioAnterior(30000.0);
        h.setPrecioNuevo(25000.0);
        h.setDescuentoAnterior(0.0);
        h.setDescuentoNuevo(0.1);
        h.setFecha(new Date());
        h.setProducto(producto);
        return h;
    }

    private ReporteProducto reporteValido(Producto producto) {
        ReporteProducto r = new ReporteProducto();
        r.setMotivo(MotivoReporte.FRAUDE);
        r.setDescripcion("El vendedor pide el pago por fuera de la plataforma");
        r.setFecha(new Date());
        r.setUsuario(vendedor);
        r.setProducto(producto);
        return r;
    }

    private Cupon cuponValido() {
        Cupon c = new Cupon();
        c.setCodigo("BIENVENIDA10");
        c.setPorcentaje(0.1);
        c.setFechaVencimiento(new Date(System.currentTimeMillis() + 30 * UN_DIA));
        c.setUsosMaximos(100);
        c.setUsosRealizados(0);
        return c;
    }

    @Test
    public void historialPrecioValidoSeGuardaTest() {
        Producto producto = productoRepo.saveAndFlush(productoValido());
        HistorialPrecio guardado = historialPrecioRepo.saveAndFlush(historialValido(producto));
        Assertions.assertNotNull(guardado.getId());
    }

    @Test
    public void historialPrecioConPrecioNegativoTest() {
        Producto producto = productoRepo.saveAndFlush(productoValido());
        HistorialPrecio h = historialValido(producto);
        h.setPrecioNuevo(-1.0);
        h.setProducto(null);

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> historialPrecioRepo.saveAndFlush(h));
        Assertions.assertEquals(Set.of("precioNuevo", "producto"), atributosInvalidos(e));
    }

    @Test
    public void reporteProductoValidoQuedaPendienteTest() {
        Producto producto = productoRepo.saveAndFlush(productoValido());
        ReporteProducto guardado = reporteProductoRepo.saveAndFlush(reporteValido(producto));
        Assertions.assertNotNull(guardado.getId());
        Assertions.assertEquals(EstadoReporte.PENDIENTE, guardado.getEstado());
        Assertions.assertNull(guardado.getAdministrador());
    }

    @Test
    public void reporteProductoSinMotivoTest() {
        Producto producto = productoRepo.saveAndFlush(productoValido());
        ReporteProducto r = reporteValido(producto);
        r.setMotivo(null);

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> reporteProductoRepo.saveAndFlush(r));
        Assertions.assertEquals(Set.of("motivo"), atributosInvalidos(e));
    }

    @Test
    public void reporteProductoRepetidoTest() {
        Producto producto = productoRepo.saveAndFlush(productoValido());
        reporteProductoRepo.saveAndFlush(reporteValido(producto));

        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> reporteProductoRepo.saveAndFlush(reporteValido(producto)));
    }

    @Test
    public void cuponValidoSeUsaEnCompraTest() {
        Cupon cupon = cuponRepo.saveAndFlush(cuponValido());

        Compra compra = new Compra();
        compra.setFechaCompra(new Date());
        compra.setMedioPago(MedioPago.PSE);
        compra.setUsuario(vendedor);
        compra.setCupon(cupon);
        Compra guardada = compraRepo.saveAndFlush(compra);

        Assertions.assertEquals(cupon.getId(), guardada.getCupon().getId());
    }

    @Test
    public void cuponConPorcentajeCeroYSinCodigoTest() {
        Cupon c = cuponValido();
        c.setPorcentaje(0.0);
        c.setCodigo("");

        ConstraintViolationException e = Assertions.assertThrows(ConstraintViolationException.class,
                () -> cuponRepo.saveAndFlush(c));
        Assertions.assertEquals(Set.of("porcentaje", "codigo"), atributosInvalidos(e));
    }

    @Test
    public void cuponConCodigoRepetidoTest() {
        cuponRepo.saveAndFlush(cuponValido());

        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> cuponRepo.saveAndFlush(cuponValido()));
    }
}
