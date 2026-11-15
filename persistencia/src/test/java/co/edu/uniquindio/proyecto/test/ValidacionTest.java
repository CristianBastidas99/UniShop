package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
}
