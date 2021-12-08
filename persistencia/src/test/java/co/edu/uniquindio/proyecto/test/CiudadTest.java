package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.repositorios.CiudadRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CiudadTest {

    @Autowired
    private CiudadRepo ciudadRepo;

    @Test
    public void registrarCiudadTest() {

        Ciudad c = new Ciudad();
        c.setNombre("Armenia");
        //Guardamos el registro
        Ciudad guardado = ciudadRepo.save(c);
        //Comprobamos que si haya quedado
        Assertions.assertNotNull(guardado);
    }

    @Test
    public void eliminarCiudadTest() {
        Ciudad c = new Ciudad();
        c.setNombre("Armenia");
        //Primero lo guardamos
        Ciudad registrado = ciudadRepo.save(c);
        //Luego lo eliminamos
        ciudadRepo.delete(registrado);
        //Por último, verificamos que si haya quedado borrado
        Ciudad buscado = ciudadRepo.findById(1L).orElse(null);
        Assertions.assertNull(buscado);
    }


    @Test
    public void actualizarCuidadTest() {
        Ciudad c = new Ciudad();
        c.setNombre("Armenia");
        //Primero lo guardamos
        Ciudad registrado = ciudadRepo.save(c);
        //Modificamos el nombre
        registrado.setNombre("Calarca");
        //Con save guardamos el registro modificado
        ciudadRepo.save(registrado);
        //Por último, verificamos que si haya quedado actualizado
        Ciudad buscado = ciudadRepo.findById(1L).orElse(null);
        Assertions.assertEquals("Calarca", buscado.getNombre());
    }

    @Test
    @Sql("classpath:ciudades.sql")
    public void listarCiudadTest() {
        //Obtenemos la lista de todos los usuarios
        List<Ciudad> lista = ciudadRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
    }

}
