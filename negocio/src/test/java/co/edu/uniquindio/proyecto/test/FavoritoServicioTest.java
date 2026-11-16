package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de favoritos (en una clase aparte para no engordar ProductoServicioTest). */
public class FavoritoServicioTest extends BaseServicioTest {

    @Autowired
    private ProductoServicio productoServicio;
    @Autowired
    private UsuarioRepo usuarioRepo;

    private Set<Long> ids(List<Producto> productos) {
        return productos.stream().map(Producto::getId).collect(Collectors.toSet());
    }

    private Set<Long> conjunto(Long... valores) {
        return new HashSet<>(Arrays.asList(valores));
    }

    private Set<Long> idsUsuariosConFavorito(Long idProducto) {
        return usuarioRepo.listarConFavorito(idProducto).stream().map(Usuario::getId).collect(Collectors.toSet());
    }

    @Test
    public void agregarYListarFavoritoTest() throws Exception {
        productoServicio.agregarFavorito(1L, 8L);
        sincronizar();
        assertEquals(conjunto(1L), ids(productoServicio.listarFavoritos(8L)));
        assertTrue(productoServicio.esFavorito(1L, 8L));
    }

    @Test
    public void agregarAUsuarioConFavoritosTest() throws Exception {
        productoServicio.agregarFavorito(3L, 5L);
        sincronizar();
        assertEquals(conjunto(2L, 3L, 5L), ids(productoServicio.listarFavoritos(5L)));
    }

    @Test
    public void agregarDosVecesNoDuplicaTest() throws Exception {
        productoServicio.agregarFavorito(1L, 8L);
        productoServicio.agregarFavorito(1L, 8L);
        sincronizar();
        productoServicio.agregarFavorito(1L, 8L);
        sincronizar();
        List<Producto> favoritos = productoServicio.listarFavoritos(8L);
        assertEquals(1, favoritos.size());
        assertEquals(1, usuarioRepo.findById(8L).orElseThrow().getMisProductosFavoritos().size());
    }

    @Test
    public void agregarYaExistenteNoHaceNadaTest() throws Exception {
        productoServicio.agregarFavorito(2L, 5L);
        sincronizar();
        assertEquals(conjunto(2L, 5L), ids(productoServicio.listarFavoritos(5L)));
        assertEquals(2, productoServicio.listarFavoritos(5L).size());
    }

    @Test
    public void quitarFavoritoTest() throws Exception {
        productoServicio.quitarFavorito(2L, 5L);
        sincronizar();
        assertEquals(conjunto(5L), ids(productoServicio.listarFavoritos(5L)));
        assertFalse(productoServicio.esFavorito(2L, 5L));
    }

    @Test
    public void quitarQueNoEstabaTest() throws Exception {
        productoServicio.quitarFavorito(1L, 8L);
        sincronizar();
        assertTrue(productoServicio.listarFavoritos(8L).isEmpty());
        productoServicio.quitarFavorito(3L, 5L);
        sincronizar();
        assertEquals(conjunto(2L, 5L), ids(productoServicio.listarFavoritos(5L)));
    }

    @Test
    public void usuarioInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.agregarFavorito(1L, 999L));
        assertEquals("El usuario no existe", e.getMessage());
        assertThrows(NegocioException.class, () -> productoServicio.agregarFavorito(1L, null));
        assertThrows(NegocioException.class, () -> productoServicio.quitarFavorito(1L, 999L));
        assertThrows(NegocioException.class, () -> productoServicio.listarFavoritos(999L));
        assertThrows(NegocioException.class, () -> productoServicio.listarFavoritos((Long) null));
    }

    @Test
    public void productoInexistenteTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.agregarFavorito(999L, 8L));
        assertEquals("El producto no existe", e.getMessage());
        assertThrows(NegocioException.class, () -> productoServicio.agregarFavorito(null, 8L));
        assertThrows(NegocioException.class, () -> productoServicio.quitarFavorito(999L, 8L));
    }

    @Test
    public void productoPropioTest() {
        // El producto 1 es de Lorena (5)
        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.agregarFavorito(1L, 5L));
        assertEquals("No puede agregar su propio producto a favoritos", e.getMessage());
    }

    @Test
    public void productoVencidoTest() throws Exception {
        // El producto 6 está vencido
        NegocioException e = assertThrows(NegocioException.class, () -> productoServicio.agregarFavorito(6L, 8L));
        assertEquals("El producto no está disponible", e.getMessage());
        sincronizar();
        assertTrue(productoServicio.listarFavoritos(8L).isEmpty());
    }

    @Test
    public void usuarioSinFavoritosTest() throws Exception {
        assertTrue(productoServicio.listarFavoritos(9L).isEmpty());
        assertFalse(productoServicio.esFavorito(1L, 9L));
    }

    @Test
    public void esFavoritoTest() {
        assertTrue(productoServicio.esFavorito(2L, 5L));
        assertFalse(productoServicio.esFavorito(3L, 5L));
        assertFalse(productoServicio.esFavorito(null, 5L));
        assertFalse(productoServicio.esFavorito(2L, null));
        assertFalse(productoServicio.esFavorito(null, null));
    }

    @Test
    public void listarConFavoritoReflejaCambiosTest() throws Exception {
        assertEquals(conjunto(5L, 7L), idsUsuariosConFavorito(2L));
        productoServicio.agregarFavorito(2L, 8L);
        productoServicio.quitarFavorito(2L, 5L);
        sincronizar();
        assertEquals(conjunto(7L, 8L), idsUsuariosConFavorito(2L));
    }
}
