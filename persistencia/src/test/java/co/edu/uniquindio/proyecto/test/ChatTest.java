package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
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
public class ChatTest {

    @Autowired
    private ChatRepo chatRepo;

    @Test
    public void registrarChatTest() {

        Ciudad cd = new Ciudad();
        cd.setNombre("Armenia");

        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail("juana@mail.com");
        u.setPassword("12345");
        u.setId_ciudad(cd);

        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFecha_limite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000);
        p.setUnidades(20);
        p.setId_ciudad(cd);
        p.setId_usuario(u);

        Chat c = new Chat();
        c.setId_producto(p);
        c.setId_usuario(u);

        //Guardamos el registro
        Chat guardado = chatRepo.save(c);
        //Comprobamos que si haya quedado
        Assertions.assertNotNull(guardado);
    }

    @Test
    public void eliminarChatTest() {

        Ciudad cd = new Ciudad();
        cd.setNombre("Armenia");

        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail("juana@mail.com");
        u.setPassword("12345");
        u.setId_ciudad(cd);

        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFecha_limite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000);
        p.setUnidades(20);
        p.setId_ciudad(cd);
        p.setId_usuario(u);

        Chat c = new Chat();
        c.setId_producto(p);
        c.setId_usuario(u);

        //Primero lo guardamos
        Chat registrado = chatRepo.save(c);
        //Luego lo eliminamos
        chatRepo.delete(registrado);
        //Por último, verificamos que si haya quedado borrado
        Chat buscado = chatRepo.findById(1L).orElse(null);
        Assertions.assertNull(buscado);
    }

    @Test
    public void actualizarChatTest() {

        Ciudad cd = new Ciudad();
        cd.setNombre("Armenia");

        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail("juana@mail.com");
        u.setPassword("12345");
        u.setId_ciudad(cd);

        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFecha_limite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000);
        p.setUnidades(20);
        p.setId_ciudad(cd);
        p.setId_usuario(u);

        Chat c = new Chat();
        c.setId_producto(p);
        c.setId_usuario(u);

        //Primero lo guardamos
        Chat registrado = chatRepo.save(c);
        //Modificamos el nombre
        u.setNombre("Pedro");
        registrado.setId_usuario(u);
        //Con save guardamos el registro modificado
        chatRepo.save(registrado);
        //Por último, verificamos que si haya quedado actualizado
        Chat buscado = chatRepo.findById(1L).orElse(null);
        Assertions.assertEquals(u, buscado.getId_usuario());
    }

    @Test
    @Sql("classpath:unishop.sql")
    public void listarChatTest() {
        //Obtenemos la l
        // ista de todos los usuarios
        List<Chat> lista = chatRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
    }

}