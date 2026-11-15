package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CategoriaTest extends CrudTestBase {

    @Autowired
    private CategoriaRepo categoriaRepo;

    @Autowired
    private ProductoRepo productoRepo;

    @Test
    public void registrarCategoriaTest() {
        Categoria c = new Categoria();
        c.setNombre("Deportes");
        c.setMisProductos(new ArrayList<>(List.of(productoRepo.findById(3L).orElseThrow())));

        Categoria guardada = categoriaRepo.saveAndFlush(c);
        sincronizar();

        Categoria buscada = categoriaRepo.findById(guardada.getId()).orElseThrow();
        Assertions.assertEquals("Deportes", buscada.getNombre());
        Assertions.assertEquals(1, buscada.getMisProductos().size());
    }

    @Test
    public void eliminarCategoriaTest() {
        // Aseo (1) tiene el producto 1: al borrarla también se borra su fila de producto_categoria
        categoriaRepo.deleteById(1L);
        sincronizar();

        Assertions.assertTrue(categoriaRepo.findById(1L).isEmpty());
        Assertions.assertEquals(1, productoRepo.findById(1L).orElseThrow().getMisCategorias().size());
    }

    @Test
    public void actualizarCategoriaTest() {
        Categoria c = categoriaRepo.findById(2L).orElseThrow();
        c.setNombre("Cocina y Comedor");
        c.getMisProductos().add(productoRepo.findById(6L).orElseThrow());
        categoriaRepo.saveAndFlush(c);
        sincronizar();

        Categoria buscada = categoriaRepo.findById(2L).orElseThrow();
        Assertions.assertEquals("Cocina y Comedor", buscada.getNombre());
        Assertions.assertEquals(2, buscada.getMisProductos().size());
    }

    @Test
    public void listarCategoriaTest() {
        Set<String> nombres = categoriaRepo.findAll().stream().map(Categoria::getNombre).collect(Collectors.toSet());
        Assertions.assertEquals(Set.of("Aseo", "Cocina", "Tecnologia", "Mascotas", "Hogar"), nombres);
    }
}
