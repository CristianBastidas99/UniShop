package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AdministradorTest {

    @Autowired
    private AdministradorRepo administradorRepo;

    @Test
    public void registrarAdministradorTest() {

        Administrador a = new Administrador();
        a.setNombre("Juanita");
        a.setEmail("juana@mail.com");
        a.setPassword("12345");
        //Guardamos el registro
        Administrador guardado = administradorRepo.save(a);
        //Comprobamos que si haya quedado
        Assertions.assertNotNull(guardado);
    }

    @Test
    public void eliminarAdministradorTest() {
        Administrador a = new Administrador();
        a.setNombre("Juanita");
        a.setEmail("juana@mail.com");
        a.setPassword("12345");
        //Primero lo guardamos
        Administrador registrado = administradorRepo.save(a);
        //Luego lo eliminamos
        administradorRepo.delete(registrado);
        //Por último, verificamos que si haya quedado borrado
        Administrador buscado = administradorRepo.findById(1L).orElse(null);
        Assertions.assertNull(buscado);
    }

    @Test
    public void actualizarAdministradorTest() {
        Administrador a = new Administrador();
        a.setNombre("Juanita");
        a.setEmail("juana@mail.com");
        a.setPassword("12345");
        //Primero lo guardamos
        Administrador registrado = administradorRepo.save(a);

        List<Administrador> lista = administradorRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);

        //Modificamos el nombre
        registrado.setNombre("Juanita Lopez");
        //Con save guardamos el registro modificado
        administradorRepo.save(registrado);

        lista = administradorRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);

        //Por último, verificamos que si haya quedado actualizado
        Administrador buscado = administradorRepo.findById(1L).orElse(null);
        Assertions.assertEquals("Juanita Lopez", buscado.getNombre());
    }

    @Test
    @Sql("classpath:unishop.sql")
    public void listarAdministradorTest() {
        //Obtenemos la l
        // ista de todos los usuarios
        List<Administrador> lista = administradorRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
    }

}
