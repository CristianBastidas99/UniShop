package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Date;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoriaTest {

    @Autowired
    private CategoriaRepo categoriaRepo;

    @Test
    public void registrarCategoriaTest() {

        Categoria cat = new Categoria();
        cat.setNombre("Aseo");
        //Guardamos el registro
        Categoria guardado = categoriaRepo.save(cat);
        //Comprobamos que si haya quedado
        Assertions.assertNotNull(guardado);
    }

    @Test
    public void eliminarCategoriaTest() {

        Categoria cat = new Categoria();
        cat.setNombre("Aseo");
        //Primero lo guardamos
        Categoria registrado = categoriaRepo.save(cat);
        //Luego lo eliminamos
        categoriaRepo.delete(registrado);
        //Por último, verificamos que si haya quedado borrado
        Categoria buscado = categoriaRepo.findById(1L).orElse(null);
        Assertions.assertNull(buscado);
    }


    @Test
    public void actualizarCategoriaTest() {

        Categoria cat = new Categoria();
        cat.setNombre("Aseo");
        //Primero lo guardamos
        Categoria registrado = categoriaRepo.save(cat);
        //Modificamos el nombre
        registrado.setNombre("Detergente");
        //Con save guardamos el registro modificado
        categoriaRepo.save(registrado);
        //Por último, verificamos que si haya quedado actualizado
        Categoria buscado = categoriaRepo.findById(1L).orElse(null);
        Assertions.assertEquals("Detergente", buscado.getNombre());
    }

    @Test
    @Sql("classpath:unishop.sql")
    public void listarCategoriaTest() {
        //Obtenemos la lista de todos los usuarios
        List<Categoria> lista = categoriaRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
    }

}

