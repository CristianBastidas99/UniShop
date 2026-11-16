package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.CiudadServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class CiudadServicioTest extends BaseServicioTest {

    @Autowired
    private CiudadServicio ciudadServicio;

    private Ciudad nuevaCiudad(String nombre) {
        Ciudad c = new Ciudad();
        c.setNombre(nombre);
        return c;
    }

    @Test
    public void registrarCiudadTest() throws Exception {
        Ciudad guardada = ciudadServicio.registrarCiudad(nuevaCiudad("Pereira"));
        sincronizar();
        assertNotNull(guardada.getId());
        assertEquals("Pereira", ciudadServicio.obtenerCiudad(guardada.getId()).getNombre());
    }

    @Test
    public void registrarNombreRepetidoTest() {
        NegocioException e = assertThrows(NegocioException.class,
                () -> ciudadServicio.registrarCiudad(nuevaCiudad("Armenia")));
        assertEquals("La ciudad Armenia ya existe", e.getMessage());
    }

    @Test
    public void actualizarCiudadTest() throws Exception {
        Ciudad modificada = nuevaCiudad("Armenia Quindio");
        modificada.setId(1L);
        ciudadServicio.actualizarCiudad(modificada);
        sincronizar();
        assertEquals("Armenia Quindio", ciudadServicio.obtenerCiudad(1L).getNombre());
    }

    @Test
    public void actualizarConNombreDeOtraTest() {
        // Objeto nuevo (no el administrado por Hibernate) para evitar el flush automático.
        Ciudad medellin = nuevaCiudad("Armenia");
        medellin.setId(2L);
        NegocioException e = assertThrows(NegocioException.class, () -> ciudadServicio.actualizarCiudad(medellin));
        assertEquals("La ciudad Armenia ya existe", e.getMessage());
    }

    @Test
    public void actualizarConIdNuloTest() {
        assertThrows(NegocioException.class, () -> ciudadServicio.actualizarCiudad(nuevaCiudad("Otra")));
    }

    @Test
    public void actualizarInexistenteTest() {
        Ciudad c = nuevaCiudad("Otra");
        c.setId(99999L);
        assertThrows(NegocioException.class, () -> ciudadServicio.actualizarCiudad(c));
    }

    @Test
    public void eliminarCiudadTest() throws Exception {
        Ciudad guardada = ciudadServicio.registrarCiudad(nuevaCiudad("Pereira"));
        sincronizar();
        Long id = guardada.getId();
        ciudadServicio.eliminarCiudad(id);
        sincronizar();
        assertThrows(NegocioException.class, () -> ciudadServicio.obtenerCiudad(id));
    }

    @Test
    public void eliminarCiudadConUsuariosOProductosTest() {
        NegocioException e = assertThrows(NegocioException.class, () -> ciudadServicio.eliminarCiudad(1L));
        assertEquals("La ciudad tiene usuarios o productos asociados y no se puede eliminar", e.getMessage());
    }

    @Test
    public void eliminarInexistenteTest() {
        assertThrows(NegocioException.class, () -> ciudadServicio.eliminarCiudad(99999L));
    }

    @Test
    public void listarCiudadesTest() {
        assertEquals(4, ciudadServicio.listarCiudades().size());
    }

    @Test
    public void obtenerInexistenteTest() {
        assertThrows(NegocioException.class, () -> ciudadServicio.obtenerCiudad(99999L));
    }

    @Test
    public void obtenerNuloTest() {
        assertThrows(NegocioException.class, () -> ciudadServicio.obtenerCiudad(null));
    }
}
