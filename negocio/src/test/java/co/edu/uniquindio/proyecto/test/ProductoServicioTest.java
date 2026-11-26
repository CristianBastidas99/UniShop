package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.HistorialPrecio;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.servicios.CiudadServicio;
import co.edu.uniquindio.proyecto.servicios.HistorialPrecioServicio;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ProductoServicioTest extends BaseServicioTest {

    private static final long DIA = 86_400_000L;

    @Autowired
    private ProductoServicio productoServicio;
    @Autowired
    private UsuarioServicio usuarioServicio;
    @Autowired
    private CiudadServicio ciudadServicio;
    @Autowired
    private HistorialPrecioServicio historialServicio;
    @Autowired
    private CategoriaRepo categoriaRepo;

    // ===== Utilidades =====

    /** Producto válido de Lorena (id 5) en Armenia (id 1), listo para publicar. */
    private Producto nuevoProducto() throws NegocioException {
        Producto p = new Producto();
        p.setNombre("Teclado");
        p.setUnidades(5);
        p.setPrecio(50000.0);
        p.setFechaLimite(new Date(System.currentTimeMillis() + DIA));
        p.setUsuario(usuarioServicio.obtenerUsuario("lorena@email.com"));
        p.setCiudad(ciudadServicio.obtenerCiudad(1L));
        List<String> imagenes = new ArrayList<>();
        imagenes.add("teclado.png");
        p.setMisImagenes(imagenes);
        return p;
    }

    /** Copia editable de un producto de la base (para enviarlo a actualizar). */
    private Producto copiaDe(Long id) throws NegocioException {
        Producto b = productoServicio.obtenerProducto(id);
        Producto c = new Producto();
        c.setId(b.getId());
        c.setNombre(b.getNombre());
        c.setDescripcion(b.getDescripcion());
        c.setUnidades(b.getUnidades());
        c.setPrecio(b.getPrecio());
        c.setDescuento(b.getDescuento());
        c.setFechaLimite(b.getFechaLimite());
        c.setCiudad(b.getCiudad());
        c.setMisImagenes(new ArrayList<>(b.getMisImagenes()));
        return c;
    }

    private Set<Long> ids(List<Producto> productos) {
        return productos.stream().map(Producto::getId).collect(Collectors.toSet());
    }

    private Set<Long> conjunto(Long... valores) {
        return new HashSet<>(Arrays.asList(valores));
    }

    private void assertPublicarFalla(Producto p, List<Long> categorias, String mensaje) {
        NegocioException e = assertThrows(NegocioException.class,
                () -> productoServicio.publicarProducto(p, categorias));
        assertEquals(mensaje, e.getMessage());
    }

    // ===== Publicar =====

    @Test
    public void publicarProductoTest() throws Exception {
        Producto guardado = productoServicio.publicarProducto(nuevoProducto(), Arrays.asList(3L, 5L));
        sincronizar();
        assertNotNull(guardado.getId(), "El producto debe tener id");

        Producto leido = productoServicio.obtenerProducto(guardado.getId());
        assertEquals("Teclado", leido.getNombre());
        assertEquals(5L, leido.getUsuario().getId());
        assertEquals(1L, leido.getCiudad().getId());
        assertEquals(Collections.singletonList("teclado.png"), leido.getMisImagenes());
        assertEquals(0.0, leido.getDescuento());

        Set<Long> categorias = leido.getMisCategorias().stream().map(Categoria::getId).collect(Collectors.toSet());
        assertEquals(conjunto(3L, 5L), categorias);
        assertTrue(categoriaRepo.findById(3L).get().getMisProductos().contains(leido));
        assertTrue(ids(productoServicio.listarVigentesPorCategoria(5L)).contains(guardado.getId()));
        assertTrue(ids(productoServicio.listarVigentes()).contains(guardado.getId()));
    }

    @Test
    public void publicarConIdTest() throws Exception {
        Producto p = nuevoProducto();
        p.setId(1L);
        assertPublicarFalla(p, Arrays.asList(1L), "El producto ya existe");
    }

    @Test
    public void publicarSinVendedorTest() throws Exception {
        Producto p = nuevoProducto();
        p.setUsuario(null);
        assertPublicarFalla(p, Arrays.asList(1L), "El vendedor es obligatorio");
    }

    @Test
    public void publicarVendedorInexistenteTest() throws Exception {
        Producto p = nuevoProducto();
        Usuario falso = new Usuario();
        falso.setId(99999L);
        p.setUsuario(falso);
        assertPublicarFalla(p, Arrays.asList(1L), "El vendedor no existe");
    }

    @Test
    public void publicarSinCiudadTest() throws Exception {
        Producto p = nuevoProducto();
        p.setCiudad(null);
        assertPublicarFalla(p, Arrays.asList(1L), "La ciudad es obligatoria");
    }

    @Test
    public void publicarCiudadInexistenteTest() throws Exception {
        Producto p = nuevoProducto();
        Ciudad falsa = new Ciudad();
        falsa.setId(99999L);
        p.setCiudad(falsa);
        assertPublicarFalla(p, Arrays.asList(1L), "La ciudad no existe");
    }

    @Test
    public void publicarSinImagenesTest() throws Exception {
        Producto p = nuevoProducto();
        p.setMisImagenes(new ArrayList<>());
        assertPublicarFalla(p, Arrays.asList(1L), "Debe subir al menos una imagen");
    }

    @Test
    public void publicarSinCategoriasTest() throws Exception {
        assertPublicarFalla(nuevoProducto(), new ArrayList<>(), "Debe elegir al menos una categoría");
        assertPublicarFalla(nuevoProducto(), null, "Debe elegir al menos una categoría");
    }

    @Test
    public void publicarCategoriaInexistenteTest() throws Exception {
        assertPublicarFalla(nuevoProducto(), Arrays.asList(1L, 99999L), "La categoría 99999 no existe");
    }

    @Test
    public void publicarCategoriaRepetidaTest() throws Exception {
        assertThrows(NegocioException.class, () -> productoServicio.publicarProducto(nuevoProducto(), Arrays.asList(1L, 1L)));
    }

    @Test
    public void publicarFechaPasadaTest() throws Exception {
        Producto p = nuevoProducto();
        p.setFechaLimite(new Date(System.currentTimeMillis() - DIA));
        assertPublicarFalla(p, Arrays.asList(1L), "La fecha límite debe ser posterior a la fecha actual");
    }

    @Test
    public void publicarUnidadesCeroTest() throws Exception {
        Producto p = nuevoProducto();
        p.setUnidades(0);
        assertPublicarFalla(p, Arrays.asList(1L), "Las unidades deben ser mayores que cero");
    }

    @Test
    public void publicarPrecioCeroTest() throws Exception {
        Producto p = nuevoProducto();
        p.setPrecio(0.0);
        assertPublicarFalla(p, Arrays.asList(1L), "El precio debe ser mayor que cero");
    }

    @Test
    public void publicarDescuentoInvalidoTest() throws Exception {
        Producto p = nuevoProducto();
        p.setDescuento(1.0);
        assertPublicarFalla(p, Arrays.asList(1L), "El descuento debe estar entre 0 y 1");
        Producto q = nuevoProducto();
        q.setDescuento(-0.1);
        assertPublicarFalla(q, Arrays.asList(1L), "El descuento debe estar entre 0 y 1");
    }

    // ===== Actualizar =====

    @Test
    public void actualizarNombreYPrecioCreaHistorialTest() throws Exception {
        assertEquals(0, historialServicio.listarPorProducto(4L).size());
        Producto cambios = copiaDe(4L);
        sincronizar();
        cambios.setNombre("Licuadora Pro");
        cambios.setPrecio(140000.0);
        productoServicio.actualizarProducto(cambios, null, 5L);
        sincronizar();

        Producto leido = productoServicio.obtenerProducto(4L);
        assertEquals("Licuadora Pro", leido.getNombre());
        assertEquals(140000.0, leido.getPrecio());
        assertEquals(5L, leido.getUsuario().getId());

        List<HistorialPrecio> historial = historialServicio.listarPorProducto(4L);
        assertEquals(1, historial.size());
        assertEquals(150000.0, historial.get(0).getPrecioAnterior());
        assertEquals(140000.0, historial.get(0).getPrecioNuevo());
        assertEquals(0.0, historial.get(0).getDescuentoAnterior());
        assertEquals(0.0, historial.get(0).getDescuentoNuevo());
    }

    @Test
    public void actualizarSoloNombreNoCreaHistorialTest() throws Exception {
        Producto cambios = copiaDe(4L);
        sincronizar();
        cambios.setNombre("Licuadora Renovada");
        productoServicio.actualizarProducto(cambios, null, 5L);
        sincronizar();
        assertEquals("Licuadora Renovada", productoServicio.obtenerProducto(4L).getNombre());
        assertEquals(0, historialServicio.listarPorProducto(4L).size());
    }

    @Test
    public void actualizarOtroUsuarioTest() throws Exception {
        Producto cambios = copiaDe(4L);
        cambios.setNombre("Robado");
        NegocioException e = assertThrows(NegocioException.class,
                () -> productoServicio.actualizarProducto(cambios, null, 6L));
        assertEquals("Solo el vendedor puede modificar el producto", e.getMessage());
    }

    @Test
    public void actualizarNoConservaVendedorEnviadoTest() throws Exception {
        Producto cambios = copiaDe(4L);
        cambios.setUsuario(usuarioServicio.obtenerUsuario("marta@email.com"));
        productoServicio.actualizarProducto(cambios, null, 5L);
        sincronizar();
        assertEquals(5L, productoServicio.obtenerProducto(4L).getUsuario().getId());
    }

    @Test
    public void actualizarProductoInexistenteTest() throws Exception {
        Producto p = nuevoProducto();
        p.setId(99999L);
        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.actualizarProducto(p, null, 5L));
        assertEquals("El producto no existe", e.getMessage());
    }

    @Test
    public void actualizarCambiaCategoriasTest() throws Exception {
        // El producto 4 está en Cocina (2) y Hogar (5): pasa a Hogar (5) y Aseo (1)
        Producto cambios = copiaDe(4L);
        sincronizar();
        productoServicio.actualizarProducto(cambios, Arrays.asList(5L, 1L), 5L);
        sincronizar();
        Producto leido = productoServicio.obtenerProducto(4L);
        Set<Long> categorias = leido.getMisCategorias().stream().map(Categoria::getId).collect(Collectors.toSet());
        assertEquals(conjunto(1L, 5L), categorias);
        assertFalse(ids(productoServicio.listarVigentesPorCategoria(2L)).contains(4L));
        assertTrue(ids(productoServicio.listarVigentesPorCategoria(1L)).contains(4L));
    }

    @Test
    public void actualizarSinImagenesTest() throws Exception {
        Producto cambios = copiaDe(4L);
        cambios.setMisImagenes(new ArrayList<>());
        NegocioException e = assertThrows(NegocioException.class,
                () -> productoServicio.actualizarProducto(cambios, null, 5L));
        assertEquals("Debe subir al menos una imagen", e.getMessage());
    }

    // ===== Descuentos =====

    @Test
    public void ponerDescuentoTest() throws Exception {
        productoServicio.ponerDescuento(4L, 5L, 0.2);
        sincronizar();
        assertEquals(0.2, productoServicio.obtenerProducto(4L).getDescuento());
        List<HistorialPrecio> historial = historialServicio.listarPorProducto(4L);
        assertEquals(1, historial.size());
        assertEquals(0.0, historial.get(0).getDescuentoAnterior());
        assertEquals(0.2, historial.get(0).getDescuentoNuevo());
        assertEquals(150000.0, historial.get(0).getPrecioNuevo());
    }

    @Test
    public void quitarDescuentoTest() throws Exception {
        // El producto 1 tiene descuento 0.3 y un historial previo en el SQL
        int antes = historialServicio.listarPorProducto(1L).size();
        productoServicio.quitarDescuento(1L, 5L);
        sincronizar();
        assertEquals(0.0, productoServicio.obtenerProducto(1L).getDescuento());
        List<HistorialPrecio> historial = historialServicio.listarPorProducto(1L);
        assertEquals(antes + 1, historial.size());
        assertEquals(0.3, historial.get(0).getDescuentoAnterior());
        assertEquals(0.0, historial.get(0).getDescuentoNuevo());
    }

    @Test
    public void descuentoOtroUsuarioTest() {
        NegocioException e1 = assertThrows(NegocioException.class, () -> productoServicio.ponerDescuento(4L, 6L, 0.2));
        assertEquals("Solo el vendedor puede modificar el producto", e1.getMessage());
        NegocioException e2 = assertThrows(NegocioException.class, () -> productoServicio.quitarDescuento(1L, 6L));
        assertEquals("Solo el vendedor puede modificar el producto", e2.getMessage());
    }

    @Test
    public void descuentoInvalidoTest() {
        for (Double invalido : new Double[]{0.0, 1.0, -0.5, 1.5, null}) {
            NegocioException e = assertThrows(NegocioException.class,
                    () -> productoServicio.ponerDescuento(4L, 5L, invalido));
            assertEquals("El descuento debe estar entre 0 y 1", e.getMessage());
        }
    }

    @Test
    public void calcularPrecioConDescuentoTest() throws Exception {
        Producto p = new Producto();
        p.setPrecio(100000.0);
        p.setDescuento(0.15);
        assertEquals(85000.0, productoServicio.calcularPrecioConDescuento(p));
        p.setDescuento(null);
        assertEquals(100000.0, productoServicio.calcularPrecioConDescuento(p));
        p.setPrecio(10.555);
        p.setDescuento(0.0);
        assertEquals(10.56, productoServicio.calcularPrecioConDescuento(p));
    }

    // ===== Consultas =====

    @Test
    public void vencidoNoApareceEnListadosPublicosTest() throws Exception {
        assertEquals(conjunto(1L, 2L, 3L, 4L, 5L), ids(productoServicio.listarVigentes()));
        assertFalse(ids(productoServicio.listarVigentesPorCategoria(5L)).contains(6L));
        assertEquals(conjunto(4L), ids(productoServicio.listarVigentesPorCategoria(5L)));
        assertFalse(ids(productoServicio.buscar("lampara", null, null, null, null, null)).contains(6L));
        assertTrue(ids(productoServicio.listarPorVendedor(6L)).contains(6L));
        assertEquals(conjunto(2L, 6L), ids(productoServicio.listarPorVendedor(6L)));
        assertTrue(ids(productoServicio.listarTodos()).contains(6L));
    }

    @Test
    public void listarVigentesPorCategoriaInexistenteTest() {
        assertThrows(NegocioException.class, () -> productoServicio.listarVigentesPorCategoria(99999L));
    }

    @Test
    public void listarPorVendedorInexistenteTest() {
        assertThrows(NegocioException.class, () -> productoServicio.listarPorVendedor(99999L));
    }

    @Test
    public void listarFavoritosTest() {
        assertEquals(conjunto(2L, 5L), ids(productoServicio.listarFavoritos("lorena@email.com")));
    }

    @Test
    public void buscarPorTextoTest() throws Exception {
        assertEquals(conjunto(2L), ids(productoServicio.buscar("SMART", null, null, null, null, null)));
        assertEquals(conjunto(2L), ids(productoServicio.buscar("  smart  ", null, null, null, null, null)));
        assertEquals(conjunto(1L, 2L, 3L, 4L, 5L), ids(productoServicio.buscar("   ", null, null, null, null, null)));
        assertEquals(conjunto(1L, 2L, 3L, 4L, 5L), ids(productoServicio.buscar(null, null, null, null, null, null)));
    }

    @Test
    public void buscarPorCategoriaTest() throws Exception {
        assertEquals(conjunto(1L, 2L, 5L), ids(productoServicio.buscar(null, 3L, null, null, null, null)));
    }

    @Test
    public void buscarPorPrecioTest() throws Exception {
        assertEquals(conjunto(2L, 4L), ids(productoServicio.buscar(null, null, 100000.0, 400000.0, null, null)));
        assertEquals(conjunto(5L), ids(productoServicio.buscar(null, null, 1000000.0, null, null, null)));
    }

    @Test
    public void buscarPorCiudadTest() throws Exception {
        assertEquals(conjunto(1L, 4L), ids(productoServicio.buscar(null, null, null, null, 1L, null)));
    }

    @Test
    public void buscarPorCalificacionTest() throws Exception {
        assertEquals(conjunto(1L, 3L), ids(productoServicio.buscar(null, null, null, null, null, 4.5)));
    }

    @Test
    public void buscarCombinadoTest() throws Exception {
        assertEquals(conjunto(1L, 5L), ids(productoServicio.buscar(null, 3L, null, null, null, 4.0)));
        assertEquals(conjunto(1L), ids(productoServicio.buscar(null, 3L, null, 100000.0, 1L, 4.0)));
        assertEquals(conjunto(1L), ids(productoServicio.buscar("cepillo", 3L, 1000.0, 100000.0, 1L, 4.0)));
    }

    @Test
    public void buscarValidacionesTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> productoServicio.buscar(null, null, 500.0, 100.0, null, null));
        assertEquals("El precio mínimo no puede ser mayor que el máximo", e.getMessage());
        assertThrows(NegocioException.class, () -> productoServicio.buscar(null, null, -1.0, null, null, null));
        assertThrows(NegocioException.class, () -> productoServicio.buscar(null, null, null, -1.0, null, null));
        assertThrows(NegocioException.class, () -> productoServicio.buscar(null, null, null, null, null, 5.5));
        assertThrows(NegocioException.class, () -> productoServicio.buscar(null, null, null, null, null, -0.5));
    }

    @Test
    public void obtenerProductoInexistenteTest() {
        NegocioException e1 = assertThrows(NegocioException.class, () -> productoServicio.obtenerProducto(99999L));
        assertEquals("El producto no existe", e1.getMessage());
        NegocioException e2 = assertThrows(NegocioException.class, () -> productoServicio.obtenerProducto(null));
        assertEquals("El producto no existe", e2.getMessage());
    }

    // ===== Eliminar =====

    @Test
    public void eliminarSinDependientesTest() throws Exception {
        Producto nuevo = productoServicio.publicarProducto(nuevoProducto(), Arrays.asList(3L));
        sincronizar();
        productoServicio.ponerDescuento(nuevo.getId(), 5L, 0.1);
        sincronizar();
        Long id = nuevo.getId();
        assertEquals(1, historialServicio.listarPorProducto(id).size());

        productoServicio.eliminarProducto(id, 5L);
        sincronizar();

        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.obtenerProducto(id));
        assertEquals("El producto no existe", e.getMessage());
        assertFalse(categoriaRepo.findById(3L).get().getMisProductos().stream().anyMatch(p -> id.equals(p.getId())));
    }

    @Test
    public void eliminarConDependientesInvalidaTest() throws Exception {
        Date antes = new Date();
        productoServicio.eliminarProducto(4L, 5L);
        sincronizar();
        Producto leido = productoServicio.obtenerProducto(4L);
        assertFalse(leido.getFechaLimite().after(new Date()), "La fecha límite debe quedar vencida");
        assertTrue(leido.getFechaLimite().getTime() >= antes.getTime() - 1000, "La fecha límite debe ser la actual");
        assertFalse(ids(productoServicio.listarVigentes()).contains(4L));
        assertTrue(ids(productoServicio.listarPorVendedor(5L)).contains(4L));
    }

    @Test
    public void eliminarOtroUsuarioTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.eliminarProducto(4L, 6L));
        assertEquals("Solo el vendedor puede eliminar el producto", e.getMessage());
    }

    @Test
    public void eliminarInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.eliminarProducto(99999L, 5L));
        assertEquals("El producto no existe", e.getMessage());
    }

    // ===== Vendedores desactivados (Marta, id 6, vende los productos 2 y 6) =====

    private Set<String> nombres(List<Producto> productos) {
        return productos.stream().map(Producto::getNombre).collect(Collectors.toSet());
    }

    @Test
    public void productosDeVendedorDesactivadoSeOcultanTest() throws Exception {
        assertTrue(nombres(productoServicio.listarVigentes()).contains("SmartPhone"));
        usuarioServicio.cambiarActivo("marta@email.com", false);
        sincronizar();

        assertFalse(nombres(productoServicio.listarVigentes()).contains("SmartPhone"));
        assertFalse(nombres(productoServicio.buscar("smart", null, null, null, null, null)).contains("SmartPhone"));
        assertTrue(productoServicio.buscar("smart", null, null, null, null, null).isEmpty());
        // Lorena (id 5) tenía de favoritos el 2 (de Marta) y el 5 (de Pedro)
        assertEquals(Collections.singleton("Portatil"), nombres(productoServicio.listarFavoritos(5L)));
        assertEquals(Collections.singleton("Portatil"), nombres(productoServicio.listarFavoritos("lorena@email.com")));
        for (Categoria c : categoriaRepo.findAll()) {
            assertFalse(nombres(productoServicio.listarVigentesPorCategoria(c.getId())).contains("SmartPhone"));
        }

        // Al reactivarlo todo vuelve
        usuarioServicio.cambiarActivo("marta@email.com", true);
        sincronizar();
        assertTrue(nombres(productoServicio.listarVigentes()).contains("SmartPhone"));
        assertEquals(2, productoServicio.listarFavoritos(5L).size());
    }

    @Test
    public void obtenerProductoVisibleDeVendedorDesactivadoNoExisteTest() throws Exception {
        assertEquals("SmartPhone", productoServicio.obtenerProductoVisible(2L).getNombre());
        usuarioServicio.cambiarActivo("marta@email.com", false);
        sincronizar();
        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.obtenerProductoVisible(2L));
        assertEquals("El producto no existe", e.getMessage());
        assertThrows(NegocioException.class, () -> productoServicio.obtenerProductoVisible(null));
        // La lectura interna (para el dueño o el administrador) sigue funcionando
        assertEquals("SmartPhone", productoServicio.obtenerProducto(2L).getNombre());
        assertTrue(nombres(productoServicio.listarTodos()).contains("SmartPhone"));
    }
}
