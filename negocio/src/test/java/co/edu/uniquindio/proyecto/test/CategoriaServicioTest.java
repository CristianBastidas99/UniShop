package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CategoriaServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriaServicioTest extends BaseServicioTest {

    @Autowired
    private CategoriaServicio categoriaServicio;

    private Categoria nuevaCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        return c;
    }

    @Test
    public void registrarCategoriaTest() throws Exception {
        Categoria guardada = categoriaServicio.registrarCategoria(nuevaCategoria("Deportes"));
        sincronizar();
        assertNotNull(guardada.getId());
        assertEquals("Deportes", categoriaServicio.obtenerCategoria(guardada.getId()).getNombre());
    }

    @Test
    public void registrarNombreRepetidoTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> categoriaServicio.registrarCategoria(nuevaCategoria("Aseo")));
        assertEquals("La categoría Aseo ya existe", e.getMessage());
    }

    @Test
    public void actualizarConNombreDeOtraTest() throws Exception {
        // Objeto nuevo (no el administrado por Hibernate): si se modificara la entidad cargada, el flush
        // automático chocaría con la restricción de nombre único antes de que el servicio valide.
        Categoria cocina = nuevaCategoria("Aseo");
        cocina.setId(2L);
        NegocioException e = assertThrows(NegocioException.class, () -> categoriaServicio.actualizarCategoria(cocina));
        assertEquals("La categoría Aseo ya existe", e.getMessage());
    }

    @Test
    public void actualizarConIdNuloTest() {
        assertThrows(NegocioException.class, () -> categoriaServicio.actualizarCategoria(nuevaCategoria("Otra")));
    }

    @Test
    public void obtenerCategoriaInexistenteTest() {
        assertThrows(NegocioException.class, () -> categoriaServicio.obtenerCategoria(99999L));
    }
}
