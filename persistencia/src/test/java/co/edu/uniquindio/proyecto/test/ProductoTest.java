package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Producto;
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
public class ProductoTest {

    @Autowired
    private ProductoRepo productoRepo;

    @Test
    public void registrarProductoTest() {

        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFecha_limite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000);
        p.setUnidades(20);
        //Guardamos el registro
        Producto guardado = productoRepo.save(p);
        //Comprobamos que si haya quedado
        Assertions.assertNotNull(guardado);
    }

    @Test
    public void eliminarproductoTest() {
        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFecha_limite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000);
        p.setUnidades(20);
        //Primero lo guardamos
        Producto registrado = productoRepo.save(p);
        //Luego lo eliminamos
        productoRepo.delete(registrado);
        //Por último, verificamos que si haya quedado borrado
        Producto buscado = productoRepo.findById(1L).orElse(null);
        Assertions.assertNull(buscado);
    }


    @Test
    public void actualizarProductoTest() {
        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFecha_limite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000);
        p.setUnidades(20);
        //Primero lo guardamos
        Producto registrado = productoRepo.save(p);
        //Modificamos el nombre
        registrado.setNombre("Cepillo Cabello");
        //Con save guardamos el registro modificado
        productoRepo.save(registrado);
        //Por último, verificamos que si haya quedado actualizado
        Producto buscado = productoRepo.findById(1L).orElse(null);
        Assertions.assertEquals("Cepillo Cabello", buscado.getNombre());
    }

    @Test
    @Sql("classpath:unishop.sql")
    public void listarProductoTest() {
        //Obtenemos la lista de todos los usuarios
        List<Producto> lista = productoRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
    }

}
