package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AdministradorTest extends CrudTestBase {

    @Autowired
    private AdministradorRepo administradorRepo;

    @Test
    public void registrarAdministradorTest() {
        Administrador a = new Administrador();
        a.setNombre("Ana Admin");
        a.setEmail("ana.admin@uniquindio.com");
        a.setPassword("clave123");

        Administrador guardado = administradorRepo.saveAndFlush(a);
        sincronizar();

        Administrador buscado = administradorRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals("Ana Admin", buscado.getNombre());
        Assertions.assertEquals("ana.admin@uniquindio.com", buscado.getEmail());
    }

    @Test
    public void eliminarAdministradorTest() {
        // El administrador 4 no revisó ningún reporte
        administradorRepo.deleteById(4L);
        sincronizar();

        Assertions.assertTrue(administradorRepo.findById(4L).isEmpty());
        Assertions.assertEquals(3, administradorRepo.count());
    }

    @Test
    public void actualizarAdministradorTest() {
        Administrador a = administradorRepo.findById(3L).orElseThrow();
        a.setNombre("Dwayne Johnson");
        administradorRepo.saveAndFlush(a);
        sincronizar();

        Assertions.assertEquals("Dwayne Johnson", administradorRepo.findById(3L).orElseThrow().getNombre());
    }

    @Test
    public void listarAdministradorTest() {
        List<Administrador> lista = administradorRepo.findAll();
        Assertions.assertEquals(4, lista.size());
        Assertions.assertTrue(administradorRepo.findByEmail("profesor@uniquindio.com").isPresent());
    }
}
