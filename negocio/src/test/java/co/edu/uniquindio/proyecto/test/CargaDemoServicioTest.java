package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.demo.ResumenCargaDemo;
import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.AdministradorServicio;
import co.edu.uniquindio.proyecto.servicios.CargaDemoServicio;
import co.edu.uniquindio.proyecto.servicios.ComentarioServicio;
import co.edu.uniquindio.proyecto.servicios.CompraServicio;
import co.edu.uniquindio.proyecto.servicios.CuponServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.ReporteProductoServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaUsuarioServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la carga de datos de demostración (con los datos de unishop.sql ya cargados). */
public class CargaDemoServicioTest extends BaseServicioTest {

    private static final String PASSWORD = "Demo1234";
    private static final String VENDEDOR_1 = "laura.tienda@unishop.com";
    private static final List<String> CORREOS_VENDEDORES = Arrays.asList(VENDEDOR_1,
            "carlos.tienda@unishop.com", "sofia.tienda@unishop.com");
    private static final List<String> CORREOS_COMPRADORES = Arrays.asList("mateo.compras@unishop.com",
            "valeria.compras@unishop.com", "santiago.compras@unishop.com");
    private static final List<String> CORREOS_ADMIN = Arrays.asList("andrea.admin@unishop.com",
            "julian.admin@unishop.com");

    @Autowired
    private CargaDemoServicio cargaDemoServicio;
    @Autowired
    private UsuarioServicio usuarioServicio;
    @Autowired
    private AdministradorServicio administradorServicio;
    @Autowired
    private ProductoServicio productoServicio;
    @Autowired
    private CompraServicio compraServicio;
    @Autowired
    private ComentarioServicio comentarioServicio;
    @Autowired
    private CuponServicio cuponServicio;
    @Autowired
    private SubastaServicio subastaServicio;
    @Autowired
    private SubastaUsuarioServicio pujaServicio;
    @Autowired
    private ReporteProductoServicio reporteServicio;

    private Set<Long> idsCompradores() throws NegocioException {
        Set<Long> ids = new HashSet<>();
        for (String correo : CORREOS_COMPRADORES) {
            ids.add(usuarioServicio.obtenerUsuario(correo).getId());
        }
        return ids;
    }

    private List<Compra> comprasDemo() throws NegocioException {
        Set<Long> compradores = idsCompradores();
        return compraServicio.listarCompra().stream()
                .filter(c -> compradores.contains(c.getUsuario().getId())).collect(Collectors.toList());
    }

    private List<Producto> productosDemo() throws NegocioException {
        List<Producto> productos = new ArrayList<>();
        for (String correo : CORREOS_VENDEDORES) {
            productos.addAll(productoServicio.listarPorVendedor(usuarioServicio.obtenerUsuario(correo).getId()));
        }
        return productos;
    }

    private List<Subasta> subastasDemo() {
        return subastaServicio.listarActivas().stream()
                .filter(s -> CORREOS_VENDEDORES.contains(s.getProducto().getUsuario().getEmail()))
                .collect(Collectors.toList());
    }

    @Test
    public void cargarCreaLosDatosTest() throws NegocioException {
        int usuariosAntes = usuarioServicio.listarUsuarios().size();
        int adminsAntes = administradorServicio.listarAdministrador().size();
        int productosAntes = productoServicio.listarTodos().size();
        int cuponesAntes = cuponServicio.listarCupones().size();
        int comprasAntes = compraServicio.listarCompra().size();
        int comentariosAntes = comentarioServicio.listarComentario().size();
        int reportesAntes = reporteServicio.listarPendientes().size();

        ResumenCargaDemo resumen = cargaDemoServicio.cargar();
        sincronizar();

        assertFalse(resumen.isYaCargado());
        assertEquals(2, resumen.getAdministradores());
        assertEquals(3, resumen.getVendedores());
        assertEquals(3, resumen.getCompradores());
        assertEquals(40, resumen.getProductos());
        assertEquals(3, resumen.getCupones());
        assertEquals(3, resumen.getSubastas());
        assertEquals(2, resumen.getReportes());
        assertEquals(30, resumen.getComentarios());
        assertEquals(10, resumen.getRespuestas());
        assertEquals(5, resumen.getComprasConCupon());

        assertEquals(adminsAntes + 2, administradorServicio.listarAdministrador().size());
        assertEquals(usuariosAntes + 6, usuarioServicio.listarUsuarios().size());
        for (String correo : CORREOS_ADMIN) {
            assertNotNull(administradorServicio.obtenerAdministrador(correo));
        }
        for (String correo : CORREOS_VENDEDORES) {
            assertNotNull(usuarioServicio.obtenerUsuario(correo).getCiudad());
        }
        for (String correo : CORREOS_COMPRADORES) {
            assertFalse(usuarioServicio.obtenerUsuario(correo).getNumeroTelefono().isEmpty());
        }
        assertEquals(productosAntes + 40, productoServicio.listarTodos().size());
        assertEquals(cuponesAntes + 3, cuponServicio.listarCupones().size());
        assertNotNull(cuponServicio.obtenerPorCodigo("UNISHOP10"));
        assertEquals(comprasAntes + resumen.getCompras(), compraServicio.listarCompra().size());
        assertTrue(resumen.getCompras() >= 22 && resumen.getCompras() <= 24);
        assertEquals(comentariosAntes + 30, comentarioServicio.listarComentario().size());
        assertEquals(reportesAntes + 2, reporteServicio.listarPendientes().size());
    }

    @Test
    public void productosConImagenYVigentesTest() throws NegocioException {
        cargaDemoServicio.cargar();
        sincronizar();

        List<Producto> productos = productosDemo();
        assertEquals(40, productos.size());
        Date ahora = new Date();
        long en60Dias = ahora.getTime() + 59L * 24 * 3600 * 1000;
        long en181Dias = ahora.getTime() + 181L * 24 * 3600 * 1000;
        for (Producto p : productos) {
            assertEquals(1, p.getMisImagenes().size());
            assertTrue(p.getMisImagenes().get(0).startsWith("resources/img/demo/"), p.getMisImagenes().get(0));
            assertTrue(p.getFechaLimite().getTime() > en60Dias && p.getFechaLimite().getTime() < en181Dias);
        }
        List<Long> vigentes = productoServicio.listarVigentes().stream().map(Producto::getId)
                .collect(Collectors.toList());
        assertTrue(vigentes.containsAll(productos.stream().map(Producto::getId).collect(Collectors.toList())));
    }

    @Test
    public void iniciarSesionConLaContrasenaDelCatalogoTest() throws NegocioException {
        cargaDemoServicio.cargar();
        sincronizar();

        assertEquals(VENDEDOR_1, usuarioServicio.iniciarSesion(VENDEDOR_1, PASSWORD).getEmail());
        assertEquals(CORREOS_ADMIN.get(0),
                administradorServicio.iniciarSesion(CORREOS_ADMIN.get(0), PASSWORD).getEmail());
    }

    @Test
    public void comprasEnVariosMesesYSinProductosEnSubastaTest() throws NegocioException {
        cargaDemoServicio.cargar();
        sincronizar();

        List<Compra> compras = comprasDemo();
        assertTrue(compras.size() >= 22);
        Date ahora = new Date();
        Set<String> meses = new HashSet<>();
        Set<Object> medios = new HashSet<>();
        Calendar calendario = Calendar.getInstance();
        for (Compra c : compras) {
            assertTrue(c.getFechaCompra().before(ahora));
            calendario.setTime(c.getFechaCompra());
            meses.add(calendario.get(Calendar.YEAR) + "-" + calendario.get(Calendar.MONTH));
            medios.add(c.getMedioPago());
        }
        assertTrue(meses.size() >= 4, "Meses distintos: " + meses);
        assertEquals(4, medios.size());

        Set<Long> productosEnSubasta = subastasDemo().stream().map(s -> s.getProducto().getId())
                .collect(Collectors.toSet());
        assertEquals(3, productosEnSubasta.size());
        for (Compra c : compras) {
            List<DetalleCompra> detalles = compraServicio.listarDetalles(c.getId());
            assertTrue(detalles.size() >= 1 && detalles.size() <= 3);
            for (DetalleCompra d : detalles) {
                assertFalse(productosEnSubasta.contains(d.getProducto().getId()));
                assertTrue(d.getUnidades() >= 1 && d.getUnidades() <= 2);
                assertTrue(d.getProducto().getUnidades() >= 1, "No debe agotar el stock");
            }
        }
    }

    @Test
    public void comentariosConPromedioYRespuestasTest() throws NegocioException {
        cargaDemoServicio.cargar();
        sincronizar();

        Set<Long> compradores = idsCompradores();
        int conRespuesta = 0;
        int altos = 0;
        List<Long> productosConComentarios = new ArrayList<>();
        for (Producto p : productosDemo()) {
            double promedio = comentarioServicio.calcularCalificacion(p.getId());
            List<?> comentarios = comentarioServicio.listarComentarioProducto(p.getId());
            if (comentarios.isEmpty()) {
                assertEquals(0, promedio);
            } else {
                assertTrue(promedio >= 2 && promedio <= 5);
                productosConComentarios.add(p.getId());
            }
        }
        for (Producto p : productosDemo()) {
            for (co.edu.uniquindio.proyecto.entidades.Comentario c : comentarioServicio.listarComentarioProducto(p.getId())) {
                assertTrue(compradores.contains(c.getUsuario().getId()));
                if (c.getRespuesta() != null) {
                    conRespuesta++;
                }
                if (c.getCalificacion() >= 4) {
                    altos++;
                }
            }
        }
        assertEquals(10, conRespuesta);
        assertTrue(altos > 15, "La mayoría debe tener 4 o 5");
        assertTrue(productosConComentarios.size() < 40, "Algunos productos quedan sin comentarios");
    }

    @Test
    public void favoritosPorCompradorTest() throws NegocioException {
        cargaDemoServicio.cargar();
        sincronizar();

        for (String correo : CORREOS_COMPRADORES) {
            Usuario u = usuarioServicio.obtenerUsuario(correo);
            int favoritos = productoServicio.listarFavoritos(u.getId()).size();
            assertTrue(favoritos >= 4 && favoritos <= 6, correo + ": " + favoritos);
        }
    }

    @Test
    public void subastasActivasConPujasCrecientesTest() throws NegocioException {
        cargaDemoServicio.cargar();
        sincronizar();

        List<Subasta> subastas = subastasDemo();
        assertEquals(3, subastas.size());
        Set<Long> vendedores = new HashSet<>();
        Set<Long> compradores = idsCompradores();
        Date ahora = new Date();
        for (Subasta s : subastas) {
            long dias = (s.getFechaLimite().getTime() - ahora.getTime()) / (24L * 3600 * 1000);
            assertTrue(dias >= 3 && dias <= 10, "Días: " + dias);
            assertTrue(s.getFechaLimite().before(s.getProducto().getFechaLimite()));
            vendedores.add(s.getProducto().getUsuario().getId());

            List<SubastaUsuario> pujas = pujaServicio.listarPorSubasta(s.getId()); // de mayor a menor
            assertTrue(pujas.size() >= 2 && pujas.size() <= 4);
            for (int i = 0; i < pujas.size() - 1; i++) {
                assertTrue(pujas.get(i).getValor() > pujas.get(i + 1).getValor());
            }
            for (SubastaUsuario p : pujas) {
                assertTrue(compradores.contains(p.getUsuario().getId()));
            }
        }
        assertEquals(3, vendedores.size());
    }

    @Test
    public void reportesPendientesDeCompradoresTest() throws NegocioException {
        cargaDemoServicio.cargar();
        sincronizar();

        Set<Long> compradores = idsCompradores();
        List<co.edu.uniquindio.proyecto.entidades.ReporteProducto> demo = reporteServicio.listarPendientes().stream()
                .filter(r -> compradores.contains(r.getUsuario().getId())).collect(Collectors.toList());
        assertEquals(2, demo.size());
        assertNotEquals(demo.get(0).getMotivo(), demo.get(1).getMotivo());
        for (co.edu.uniquindio.proyecto.entidades.ReporteProducto r : demo) {
            assertNotNull(r.getDescripcion());
        }
    }

    @Test
    public void cargarDosVecesNoDuplicaTest() throws NegocioException {
        cargaDemoServicio.cargar();
        sincronizar();
        int usuarios = usuarioServicio.listarUsuarios().size();
        int productos = productoServicio.listarTodos().size();
        int compras = compraServicio.listarCompra().size();
        int comentarios = comentarioServicio.listarComentario().size();
        int cupones = cuponServicio.listarCupones().size();

        ResumenCargaDemo segunda = cargaDemoServicio.cargar();
        sincronizar();

        assertTrue(segunda.isYaCargado());
        assertEquals(0, segunda.getProductos());
        assertEquals(usuarios, usuarioServicio.listarUsuarios().size());
        assertEquals(productos, productoServicio.listarTodos().size());
        assertEquals(compras, compraServicio.listarCompra().size());
        assertEquals(comentarios, comentarioServicio.listarComentario().size());
        assertEquals(cupones, cuponServicio.listarCupones().size());
    }
}
