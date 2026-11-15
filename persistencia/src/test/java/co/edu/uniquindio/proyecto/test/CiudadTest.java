package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.repositorios.CiudadRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

public class CiudadTest extends CrudTestBase {

    @Autowired
    private CiudadRepo ciudadRepo;

    private Ciudad nuevaCiudad(String nombre) {
        Ciudad c = new Ciudad();
        c.setNombre(nombre);
        return c;
    }

    @Test
    public void registrarCiudadTest() {
        Ciudad guardada = ciudadRepo.saveAndFlush(nuevaCiudad("Pereira"));
        sincronizar();

        Assertions.assertEquals("Pereira", ciudadRepo.findById(guardada.getId()).orElseThrow().getNombre());
    }

    @Test
    public void eliminarCiudadTest() {
        // Todas las ciudades del SQL tienen usuarios o productos, así que se elimina una nueva
        Ciudad guardada = ciudadRepo.saveAndFlush(nuevaCiudad("Manizales"));
        ciudadRepo.delete(guardada);
        sincronizar();

        Assertions.assertTrue(ciudadRepo.findById(guardada.getId()).isEmpty());
        Assertions.assertEquals(4, ciudadRepo.count());
    }

    @Test
    public void actualizarCiudadTest() {
        Ciudad c = ciudadRepo.findById(3L).orElseThrow();
        c.setNombre("Bogota D.C.");
        ciudadRepo.saveAndFlush(c);
        sincronizar();

        Assertions.assertEquals("Bogota D.C.", ciudadRepo.findById(3L).orElseThrow().getNombre());
    }

    @Test
    public void listarCiudadTest() {
        Set<String> nombres = ciudadRepo.findAll().stream().map(Ciudad::getNombre).collect(Collectors.toSet());
        Assertions.assertEquals(Set.of("Armenia", "Medellin", "Bogota", "Cartagena"), nombres);
    }
}
