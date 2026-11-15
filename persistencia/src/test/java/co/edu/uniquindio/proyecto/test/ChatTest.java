package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.CiudadRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
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
public class ChatTest {

    @Autowired
    private CiudadRepo ciudadRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private ProductoRepo productoRepo;

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
        u.setCiudad(cd);

        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFechaLimite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000.0);
        p.setUnidades(20);
        p.setCiudad(cd);
        p.setUsuario(u);

        // Las entidades relacionadas deben existir en la base antes de usarlas
        ciudadRepo.save(cd);
        usuarioRepo.save(u);
        p.setMisImagenes(List.of("uploads/cepillo.png"));
        productoRepo.save(p);

        Chat c = new Chat();
        c.setProducto(p);
        c.setUsuario(u);

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
        u.setCiudad(cd);

        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFechaLimite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000.0);
        p.setUnidades(20);
        p.setCiudad(cd);
        p.setUsuario(u);

        // Las entidades relacionadas deben existir en la base antes de usarlas
        ciudadRepo.save(cd);
        usuarioRepo.save(u);
        p.setMisImagenes(List.of("uploads/cepillo.png"));
        productoRepo.save(p);

        Chat c = new Chat();
        c.setProducto(p);
        c.setUsuario(u);

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
        u.setCiudad(cd);

        Producto p = new Producto();
        p.setDescripcion("Es el mejor producto que veras");
        p.setDescuento(0.0);
        p.setFechaLimite(new Date(112, 6, 36));
        p.setNombre("Cepillo Electrico");
        p.setPrecio(30000.0);
        p.setUnidades(20);
        p.setCiudad(cd);
        p.setUsuario(u);

        // Las entidades relacionadas deben existir en la base antes de usarlas
        ciudadRepo.save(cd);
        usuarioRepo.save(u);
        p.setMisImagenes(List.of("uploads/cepillo.png"));
        productoRepo.save(p);

        Chat c = new Chat();
        c.setProducto(p);
        c.setUsuario(u);

        //Primero lo guardamos
        Chat registrado = chatRepo.save(c);
        //Modificamos el nombre
        u.setNombre("Pedro");
        registrado.setUsuario(u);
        //Con save guardamos el registro modificado
        chatRepo.save(registrado);
        //Por último, verificamos que si haya quedado actualizado
        Chat buscado = chatRepo.findById(1L).orElse(null);
        Assertions.assertEquals(u, buscado.getUsuario());
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