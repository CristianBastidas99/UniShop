package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.*;
import co.edu.uniquindio.proyecto.repositorios.ChatRepo;
import co.edu.uniquindio.proyecto.repositorios.MensajeRepo;
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
public class MensajeTest {

    @Autowired
    private MensajeRepo mensajeRepo;

    @Test
    public void registrarMensajeTest() {

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

        Mensaje m = new Mensaje();
        m.setMensaje("Disponible?");
        m.setFecha(new Date(115, 6, 36));
        m.setId_chat(c);

        //Guardamos el registro
        Mensaje guardado = mensajeRepo.save(m);
        //Comprobamos que si haya quedado
        Assertions.assertNotNull(guardado);
    }

    @Test
    public void eliminarMensajeTest() {

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

        Mensaje m = new Mensaje();
        m.setMensaje("Disponible?");
        m.setFecha(new Date(115, 6, 36));
        m.setId_chat(c);

        //Primero lo guardamos
        Mensaje registrado = mensajeRepo.save(m);
        //Luego lo eliminamos
        mensajeRepo.delete(registrado);
        //Por último, verificamos que si haya quedado borrado
        Mensaje buscado = mensajeRepo.findById(1L).orElse(null);
        Assertions.assertNull(buscado);
    }

    @Test
    public void actualizarMensajeTest() {

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

        Mensaje m = new Mensaje();
        m.setMensaje("Disponible?");
        m.setFecha(new Date(115, 6, 36));
        m.setId_chat(c);

        //Primero lo guardamos
        Mensaje registrado = mensajeRepo.save(m);
        //Modificamos el nombre
        m.setMensaje("Cuanto dura la bateria?");
        //Con save guardamos el registro modificado
        mensajeRepo.save(registrado);
        //Por último, verificamos que si haya quedado actualizado
        Mensaje buscado = mensajeRepo.findById(1L).orElse(null);
        Assertions.assertEquals("Cuanto dura la bateria?", buscado.getMensaje());
    }

    @Test
    @Sql("classpath:unishop.sql")
    public void listarMensajeTest() {
        //Obtenemos la l
        // ista de todos los usuarios
        List<Mensaje> lista = mensajeRepo.findAll();
        //Imprimimos la lista
        System.out.println(lista);
    }

}
