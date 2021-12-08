package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.NegocioApplication;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NegocioApplication.class)
@Transactional
public class UsuarioServicioTest {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Test
    public void registrarUsuarioTest(){
        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail("juana@mail.com");
        u.setPassword("12345");
        try {
            //Guardamos el registro
            Usuario guardado = usuarioServicio.registrarUsuario(u);
            //Comprobamos que si haya quedado
            Assert.assertNotNull(guardado);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void actualizarUsuarioTest() {
        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail("juana@mail.com");
        u.setPassword("12345");
        try {
            //Guardamos el registro
            Usuario registrado = usuarioServicio.registrarUsuario(u);
            //Modificamos el nombre
            registrado.setNombre("Juanita Lopez");
            //Con save guardamos el registro modificado
            usuarioServicio.actualizarUsuario(registrado);
            //Por último, verificamos que si haya quedado actualizado
            Usuario buscado = usuarioServicio.obtenerUsuario("juana@mail.com");
            Assertions.assertEquals("Juanita Lopez", buscado.getNombre());;
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Test
    public void eliminarUsuarioTest() {

        Usuario u = new Usuario();
        u.setNombre("Juanita");
        u.setEmail("juana@mail.com");
        u.setPassword("12345");

        try {
            //Guardamos el registro
            Usuario guardado = usuarioServicio.registrarUsuario(u);
            //Luego lo eliminamos
            usuarioServicio.eliminarUsuario(u.getEmail());
            //Por último, verificamos que si haya quedado borrado
            Usuario buscado = usuarioServicio.obtenerUsuario("juana@mail.com");
            Assertions.assertNull(buscado);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    @Sql("classpath:unishop.sql")
    public void listarUsuariosTest() {
        //Obtenemos la lista de todos los usuarios
        List<Usuario> lista = usuarioServicio.listarUsuarios();
        //Imprimimos la lista
        System.out.println(lista);
    }

}
