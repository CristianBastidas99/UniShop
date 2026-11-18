package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.CantidadDTO;
import co.edu.uniquindio.proyecto.dto.MedioPagoDTO;
import co.edu.uniquindio.proyecto.dto.PromedioDTO;
import co.edu.uniquindio.proyecto.dto.VentaMesDTO;
import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pruebas de las consultas de los repositorios (Paso 1.4).
 * Antes de cada prueba se arma un conjunto de datos conocido y cada prueba verifica el resultado exacto.
 *
 * Datos:
 * - Vendedores: Ana (Armenia) y Beto (Pereira). Compradora: Carla (Armenia).
 * - Productos: Celular (Ana, Tecnologia, vigente), Cepillo Electrico (Beto, Hogar + Tecnologia, vigente),
 *   Lampara (Ana, Hogar, vencida) y Mesa (Beto, Hogar, sin unidades). Deportes no tiene productos.
 * - Calificaciones: Celular 5, 4 y 3 (promedio 4); Cepillo 2.
 * - Compras: enero/2026 (Carla, TARJETA, 2 celulares + 1 cepillo = 1.650.000),
 *   febrero/2026 (Carla, PSE, 3 cepillos a 45.000 = 135.000; Beto, TARJETA, 1 celular = 800.000).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ConsultaTest {

    private static final long UN_DIA = 24L * 60 * 60 * 1000;

    @Autowired private CiudadRepo ciudadRepo;
    @Autowired private UsuarioRepo usuarioRepo;
    @Autowired private AdministradorRepo administradorRepo;
    @Autowired private CategoriaRepo categoriaRepo;
    @Autowired private ProductoRepo productoRepo;
    @Autowired private ComentarioRepo comentarioRepo;
    @Autowired private CompraRepo compraRepo;
    @Autowired private DetalleCompraRepo detalleCompraRepo;
    @Autowired private ChatRepo chatRepo;
    @Autowired private MensajeRepo mensajeRepo;
    @Autowired private SubastaRepo subastaRepo;
    @Autowired private SubastaUsuarioRepo subastaUsuarioRepo;
    @Autowired private CuponRepo cuponRepo;
    @Autowired private ReporteProductoRepo reporteProductoRepo;
    @Autowired private HistorialPrecioRepo historialPrecioRepo;

    private Ciudad armenia, pereira;
    private Usuario ana, beto, carla;
    private Categoria tecnologia, hogar;
    private Producto celular, cepillo, lampara, mesa;
    private Compra compraEnero, compraFebreroCarla;
    private Chat chatCelular;
    private Subasta subastaActiva, subastaVencida;
    private Date hoy;

    // ===== Datos de prueba =====

    @BeforeEach
    public void crearDatos() {
        hoy = new Date();

        armenia = ciudad("Armenia");
        pereira = ciudad("Pereira");

        ana = usuario("Ana", "ana@mail.com", armenia);
        beto = usuario("Beto", "beto@mail.com", pereira);
        carla = usuario("Carla", "carla@mail.com", armenia);

        Administrador admin = new Administrador();
        admin.setNombre("Admin");
        admin.setEmail("admin@unishop.com");
        admin.setPassword("admin123");
        administradorRepo.saveAndFlush(admin);

        celular = producto("Celular", 800000.0, 5, en(30), ana, armenia);
        cepillo = producto("Cepillo Electrico", 50000.0, 10, en(30), beto, pereira);
        lampara = producto("Lampara", 120000.0, 3, en(-1), ana, armenia);
        mesa = producto("Mesa", 300000.0, 0, en(30), beto, pereira);

        tecnologia = categoria("Tecnologia", celular, cepillo);
        hogar = categoria("Hogar", cepillo, lampara, mesa);
        categoria("Deportes");

        carla.setMisProductosFavoritos(new ArrayList<>(List.of(celular, cepillo)));
        carla = usuarioRepo.saveAndFlush(carla);

        comentario(celular, carla, 5, en(-3));
        comentario(celular, beto, 4, en(-2));
        comentario(celular, carla, 3, en(-1));
        comentario(cepillo, carla, 2, en(-1));

        compraEnero = compra(carla, MedioPago.TARJETA, fecha(2026, 1, 15));
        detalle(compraEnero, celular, 2, 800000.0);
        detalle(compraEnero, cepillo, 1, 50000.0);
        compraFebreroCarla = compra(carla, MedioPago.PSE, fecha(2026, 2, 10));
        detalle(compraFebreroCarla, cepillo, 3, 45000.0);
        Compra compraFebreroBeto = compra(beto, MedioPago.TARJETA, fecha(2026, 2, 20));
        detalle(compraFebreroBeto, celular, 1, 800000.0);

        chatCelular = chat(carla, celular);
        chat(beto, lampara);
        // Se guardan en desorden para verificar que la consulta los ordena por fecha
        mensaje(chatCelular, "Sigue disponible?", fecha(2026, 3, 1, 10));
        mensaje(chatCelular, "Hola", fecha(2026, 3, 1, 9));

        subastaActiva = subasta(cepillo, en(2));
        subastaVencida = subasta(lampara, en(-1));
        puja(subastaActiva, carla, 60000.0, en(-1));
        puja(subastaActiva, ana, 70000.0, hoy);

        cupon("VIGENTE", en(10), 5, 1);
        cupon("VENCIDO", en(-1), 5, 0);
        cupon("AGOTADO", en(10), 5, 5);

        reporte(carla, mesa, EstadoReporte.PENDIENTE);
        reporte(ana, cepillo, EstadoReporte.RECHAZADO);

        historial(cepillo, 60000.0, 55000.0, en(-10));
        historial(cepillo, 55000.0, 50000.0, en(-5));
    }

    // ===== ProductoRepo =====

    @Test
    public void listarVigentesTest() {
        Assertions.assertEquals(Set.of("Celular", "Cepillo Electrico"), nombres(productoRepo.listarVigentes(hoy)));
    }

    @Test
    public void listarVigentesPorCategoriaTest() {
        Assertions.assertEquals(Set.of("Cepillo Electrico"),
                nombres(productoRepo.listarVigentesPorCategoria(hogar.getId(), hoy)));
        Assertions.assertEquals(Set.of("Celular", "Cepillo Electrico"),
                nombres(productoRepo.listarVigentesPorCategoria(tecnologia.getId(), hoy)));
    }

    @Test
    public void buscarPorTextoTest() {
        Assertions.assertEquals(Set.of("Celular"), nombres(buscar("CEL", null, null, null, null, null)));
        Assertions.assertEquals(Set.of("Celular", "Cepillo Electrico"), nombres(buscar("c", null, null, null, null, null)));
        // Lampara contiene "a" pero está vencida; Mesa no tiene unidades
        Assertions.assertEquals(Set.of("Celular", "Cepillo Electrico"), nombres(buscar(null, null, null, null, null, null)));
        Assertions.assertTrue(buscar("lampara", null, null, null, null, null).isEmpty());
    }

    @Test
    public void buscarConFiltrosTest() {
        Assertions.assertEquals(Set.of("Cepillo Electrico"), nombres(buscar(null, hogar.getId(), null, null, null, null)));
        Assertions.assertEquals(Set.of("Celular"), nombres(buscar(null, null, 100000.0, 1000000.0, null, null)));
        Assertions.assertEquals(Set.of("Cepillo Electrico"), nombres(buscar(null, null, null, 50000.0, null, null)));
        Assertions.assertEquals(Set.of("Cepillo Electrico"), nombres(buscar(null, null, null, null, pereira.getId(), null)));
        Assertions.assertEquals(Set.of("Celular"), nombres(buscar(null, null, null, null, null, 3.5)));
        Assertions.assertEquals(Set.of("Celular", "Cepillo Electrico"), nombres(buscar(null, null, null, null, null, 0.0)));
        // Varios filtros a la vez
        Assertions.assertEquals(Set.of("Celular"),
                nombres(buscar("e", tecnologia.getId(), null, null, armenia.getId(), 4.0)));
        Assertions.assertTrue(buscar("cepillo", tecnologia.getId(), null, null, armenia.getId(), null).isEmpty());
    }

    @Test
    public void listarFavoritosTest() {
        Assertions.assertEquals(Set.of("Celular", "Cepillo Electrico"), nombres(productoRepo.listarFavoritos("carla@mail.com")));
        // La consulta anterior devolvía todos los productos si el email existía
        Assertions.assertTrue(productoRepo.listarFavoritos("ana@mail.com").isEmpty());
        Assertions.assertTrue(productoRepo.listarFavoritos("noexiste@mail.com").isEmpty());
    }

    @Test
    public void listarPorVendedorTest() {
        Assertions.assertEquals(Set.of("Celular", "Lampara"), nombres(productoRepo.listarPorVendedor(ana.getId())));
        Assertions.assertTrue(productoRepo.listarPorVendedor(carla.getId()).isEmpty());
    }

    // ===== ComentarioRepo =====

    @Test
    public void calificacionPromedioTest() {
        Assertions.assertEquals(4.0, comentarioRepo.calificacionPromedio(celular.getId()));
        Assertions.assertEquals(2.0, comentarioRepo.calificacionPromedio(cepillo.getId()));
        Assertions.assertNull(comentarioRepo.calificacionPromedio(mesa.getId()));
    }

    @Test
    public void calificacionPromedioPorProductosTest() {
        List<Object[]> filas = comentarioRepo.calificacionPromedioPorProductos(
                List.of(celular.getId(), cepillo.getId(), mesa.getId()));
        // Mesa no tiene comentarios: no aparece
        Assertions.assertEquals(2, filas.size());
        java.util.Map<Long, Double> promedios = new java.util.HashMap<>();
        for (Object[] fila : filas) {
            promedios.put((Long) fila[0], (Double) fila[1]);
        }
        Assertions.assertEquals(4.0, promedios.get(celular.getId()));
        Assertions.assertEquals(2.0, promedios.get(cepillo.getId()));
        Assertions.assertFalse(promedios.containsKey(mesa.getId()));
    }

    @Test
    public void listarComentariosTest() {
        List<Comentario> delCelular = comentarioRepo.listarPorProducto(celular.getId());
        Assertions.assertEquals(List.of(3, 4, 5),
                delCelular.stream().map(Comentario::getCalificacion).collect(Collectors.toList()));
        Assertions.assertEquals(3, comentarioRepo.listarPorUsuario(carla.getId()).size());
        Assertions.assertEquals(1, comentarioRepo.listarPorUsuario(beto.getId()).size());
    }

    // ===== Compras =====

    @Test
    public void listarComprasPorUsuarioTest() {
        List<Compra> deCarla = compraRepo.listarPorUsuario(carla.getId());
        Assertions.assertEquals(List.of(compraFebreroCarla.getId(), compraEnero.getId()),
                deCarla.stream().map(Compra::getId).collect(Collectors.toList()));
        Assertions.assertTrue(compraRepo.listarPorUsuario(ana.getId()).isEmpty());
    }

    @Test
    public void listarDetallesPorCompraTest() {
        Assertions.assertEquals(Set.of("Celular", "Cepillo Electrico"),
                detalleCompraRepo.listarPorCompra(compraEnero.getId()).stream()
                        .map(d -> d.getProducto().getNombre()).collect(Collectors.toSet()));
    }

    // ===== Chat y mensajes =====

    @Test
    public void listarChatsTest() {
        Assertions.assertEquals(1, chatRepo.listarPorProducto(celular.getId()).size());
        Assertions.assertEquals(2, chatRepo.listarPorUsuario(ana.getId()).size()); // vendedora de los dos productos
        Assertions.assertEquals(1, chatRepo.listarPorUsuario(carla.getId()).size()); // compradora
        Assertions.assertTrue(chatRepo.buscarPorCompradorYProducto(carla.getId(), celular.getId()).isPresent());
        Assertions.assertTrue(chatRepo.buscarPorCompradorYProducto(carla.getId(), cepillo.getId()).isEmpty());
    }

    @Test
    public void listarMensajesPorChatTest() {
        Assertions.assertEquals(List.of("Hola", "Sigue disponible?"),
                mensajeRepo.listarPorChat(chatCelular.getId()).stream()
                        .map(Mensaje::getMensaje).collect(Collectors.toList()));
    }

    // ===== Login =====

    @Test
    public void loginUsuarioTest() {
        Assertions.assertTrue(usuarioRepo.buscarPorEmailYPassword("ana@mail.com", "12345").isPresent());
        Assertions.assertTrue(usuarioRepo.buscarPorEmailYPassword("ana@mail.com", "otra").isEmpty());
        Assertions.assertTrue(usuarioRepo.buscarPorEmailYPassword("admin@unishop.com", "admin123").isEmpty());
    }

    @Test
    public void loginAdministradorTest() {
        Assertions.assertTrue(administradorRepo.buscarPorEmailYPassword("admin@unishop.com", "admin123").isPresent());
        Assertions.assertTrue(administradorRepo.buscarPorEmailYPassword("admin@unishop.com", "12345").isEmpty());
        Assertions.assertTrue(administradorRepo.buscarPorEmailYPassword("ana@mail.com", "12345").isEmpty());
    }

    // ===== Subastas =====

    @Test
    public void listarSubastasActivasTest() {
        List<Subasta> activas = subastaRepo.listarActivas(hoy);
        Assertions.assertEquals(1, activas.size());
        Assertions.assertEquals(subastaActiva.getId(), activas.get(0).getId());
    }

    @Test
    public void pujaMasAltaTest() {
        Assertions.assertEquals(70000.0, subastaUsuarioRepo.pujaMasAlta(subastaActiva.getId()));
        Assertions.assertNull(subastaUsuarioRepo.pujaMasAlta(subastaVencida.getId()));

        List<SubastaUsuario> pujas = subastaUsuarioRepo.listarPorSubasta(subastaActiva.getId());
        Assertions.assertEquals(2, pujas.size());
        Assertions.assertEquals("Ana", pujas.get(0).getUsuario().getNombre());
    }

    // ===== Necesidades extra =====

    @Test
    public void cuponDisponibleTest() {
        Assertions.assertTrue(cuponRepo.buscarDisponible("VIGENTE", hoy).isPresent());
        Assertions.assertTrue(cuponRepo.buscarDisponible("VENCIDO", hoy).isEmpty());
        Assertions.assertTrue(cuponRepo.buscarDisponible("AGOTADO", hoy).isEmpty());
        Assertions.assertTrue(cuponRepo.buscarDisponible("NOEXISTE", hoy).isEmpty());
        Assertions.assertTrue(cuponRepo.findByCodigo("AGOTADO").isPresent());
    }

    @Test
    public void listarReportesProductoTest() {
        List<ReporteProducto> pendientes = reporteProductoRepo.listarPorEstado(EstadoReporte.PENDIENTE);
        Assertions.assertEquals(1, pendientes.size());
        Assertions.assertEquals("Mesa", pendientes.get(0).getProducto().getNombre());
        Assertions.assertEquals(1, reporteProductoRepo.listarPorProducto(cepillo.getId()).size());
        Assertions.assertTrue(reporteProductoRepo.listarPorProducto(celular.getId()).isEmpty());
    }

    @Test
    public void listarHistorialPrecioTest() {
        List<HistorialPrecio> historial = historialPrecioRepo.listarPorProducto(cepillo.getId());
        Assertions.assertEquals(List.of(50000.0, 55000.0),
                historial.stream().map(HistorialPrecio::getPrecioNuevo).collect(Collectors.toList()));
    }

    // ===== Reportes del administrador (Fase 9) =====

    @Test
    public void reporteProductosPorCategoriaTest() {
        Assertions.assertEquals(List.of(
                        new CantidadDTO("Deportes", 0L),
                        new CantidadDTO("Hogar", 3L),
                        new CantidadDTO("Tecnologia", 2L)),
                productoRepo.contarPorCategoria());
    }

    @Test
    public void reporteVentasPorMesTest() {
        Assertions.assertEquals(List.of(
                        new VentaMesDTO(2026, 1, 1650000.0),
                        new VentaMesDTO(2026, 2, 935000.0)),
                detalleCompraRepo.ventasPorMes());
    }

    @Test
    public void reporteTopVendedoresTest() {
        Assertions.assertEquals(List.of(new CantidadDTO("Beto", 4L), new CantidadDTO("Ana", 3L)),
                detalleCompraRepo.topVendedores(PageRequest.of(0, 5)));
        Assertions.assertEquals(List.of(new CantidadDTO("Beto", 4L)),
                detalleCompraRepo.topVendedores(PageRequest.of(0, 1)));
    }

    @Test
    public void reporteMejorCalificadosTest() {
        Assertions.assertEquals(List.of(new PromedioDTO("Celular", 4.0), new PromedioDTO("Cepillo Electrico", 2.0)),
                productoRepo.mejorCalificados(PageRequest.of(0, 5)));
    }

    @Test
    public void reporteComprasPorMedioPagoTest() {
        Assertions.assertEquals(List.of(new MedioPagoDTO(MedioPago.TARJETA, 2L), new MedioPagoDTO(MedioPago.PSE, 1L)),
                compraRepo.contarPorMedioPago());
    }

    // ===== Utilidades =====

    private List<Producto> buscar(String texto, Long idCategoria, Double precioMin, Double precioMax,
                                  Long idCiudad, Double calificacionMin) {
        return productoRepo.buscar(texto, idCategoria, precioMin, precioMax, idCiudad, calificacionMin, hoy);
    }

    private Set<String> nombres(List<Producto> productos) {
        return productos.stream().map(Producto::getNombre).collect(Collectors.toSet());
    }

    /** Fecha a {@code dias} días de hoy (negativo = en el pasado). */
    private Date en(int dias) {
        return new Date(hoy.getTime() + dias * UN_DIA);
    }

    private Date fecha(int anio, int mes, int dia) {
        return fecha(anio, mes, dia, 12);
    }

    private Date fecha(int anio, int mes, int dia, int hora) {
        return Date.from(LocalDateTime.of(anio, mes, dia, hora, 0).atZone(ZoneId.systemDefault()).toInstant());
    }

    private Ciudad ciudad(String nombre) {
        Ciudad c = new Ciudad();
        c.setNombre(nombre);
        return ciudadRepo.saveAndFlush(c);
    }

    private Usuario usuario(String nombre, String email, Ciudad ciudad) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword("12345");
        u.setCiudad(ciudad);
        return usuarioRepo.saveAndFlush(u);
    }

    private Producto producto(String nombre, Double precio, int unidades, Date fechaLimite, Usuario vendedor, Ciudad ciudad) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setUnidades(unidades);
        p.setFechaLimite(fechaLimite);
        p.setUsuario(vendedor);
        p.setCiudad(ciudad);
        p.setMisImagenes(new ArrayList<>(List.of("uploads/" + nombre.toLowerCase() + ".png")));
        return productoRepo.saveAndFlush(p);
    }

    private Categoria categoria(String nombre, Producto... productos) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setMisProductos(new ArrayList<>(List.of(productos)));
        return categoriaRepo.saveAndFlush(c);
    }

    private void comentario(Producto producto, Usuario usuario, int calificacion, Date fecha) {
        Comentario c = new Comentario();
        c.setProducto(producto);
        c.setUsuario(usuario);
        c.setCalificacion(calificacion);
        c.setMensaje("Calificación " + calificacion);
        c.setFecha(fecha);
        comentarioRepo.saveAndFlush(c);
    }

    private Compra compra(Usuario usuario, MedioPago medioPago, Date fecha) {
        Compra c = new Compra();
        c.setUsuario(usuario);
        c.setMedioPago(medioPago);
        c.setFechaCompra(fecha);
        return compraRepo.saveAndFlush(c);
    }

    private void detalle(Compra compra, Producto producto, int unidades, Double precio) {
        DetalleCompra d = new DetalleCompra();
        d.setCompra(compra);
        d.setProducto(producto);
        d.setUnidades(unidades);
        d.setPrecioProducto(precio);
        detalleCompraRepo.saveAndFlush(d);
    }

    private Chat chat(Usuario comprador, Producto producto) {
        Chat c = new Chat();
        c.setUsuario(comprador);
        c.setProducto(producto);
        return chatRepo.saveAndFlush(c);
    }

    private void mensaje(Chat chat, String texto, Date fecha) {
        Mensaje m = new Mensaje();
        m.setChat(chat);
        m.setRemitente(chat.getUsuario());
        m.setMensaje(texto);
        m.setFecha(fecha);
        mensajeRepo.saveAndFlush(m);
    }

    private Subasta subasta(Producto producto, Date fechaLimite) {
        Subasta s = new Subasta();
        s.setProducto(producto);
        s.setFechaLimite(fechaLimite);
        return subastaRepo.saveAndFlush(s);
    }

    private void puja(Subasta subasta, Usuario usuario, Double valor, Date fecha) {
        SubastaUsuario su = new SubastaUsuario();
        su.setSubasta(subasta);
        su.setUsuario(usuario);
        su.setValor(valor);
        su.setFechaSubasta(fecha);
        subastaUsuarioRepo.saveAndFlush(su);
    }

    private void cupon(String codigo, Date vencimiento, int usosMaximos, int usosRealizados) {
        Cupon c = new Cupon();
        c.setCodigo(codigo);
        c.setPorcentaje(0.1);
        c.setFechaVencimiento(vencimiento);
        c.setUsosMaximos(usosMaximos);
        c.setUsosRealizados(usosRealizados);
        cuponRepo.saveAndFlush(c);
    }

    private void reporte(Usuario usuario, Producto producto, EstadoReporte estado) {
        ReporteProducto r = new ReporteProducto();
        r.setUsuario(usuario);
        r.setProducto(producto);
        r.setMotivo(MotivoReporte.FRAUDE);
        r.setEstado(estado);
        r.setFecha(hoy);
        reporteProductoRepo.saveAndFlush(r);
    }

    private void historial(Producto producto, Double precioAnterior, Double precioNuevo, Date fecha) {
        HistorialPrecio h = new HistorialPrecio();
        h.setProducto(producto);
        h.setPrecioAnterior(precioAnterior);
        h.setPrecioNuevo(precioNuevo);
        h.setFecha(fecha);
        historialPrecioRepo.saveAndFlush(h);
    }
}
