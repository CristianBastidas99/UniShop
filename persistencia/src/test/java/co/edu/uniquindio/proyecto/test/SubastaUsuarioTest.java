package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.repositorios.SubastaUsuarioRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class SubastaUsuarioTest extends CrudTestBase {

    @Autowired
    private SubastaUsuarioRepo subastaUsuarioRepo;

    @Autowired
    private SubastaRepo subastaRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Test
    public void registrarSubastaUsuarioTest() {
        SubastaUsuario puja = new SubastaUsuario();
        puja.setValor(2200000.0);
        puja.setFechaSubasta(new Date());
        puja.setSubasta(subastaRepo.findById(1L).orElseThrow());
        puja.setUsuario(usuarioRepo.findById(9L).orElseThrow());

        SubastaUsuario guardada = subastaUsuarioRepo.saveAndFlush(puja);
        sincronizar();

        SubastaUsuario buscada = subastaUsuarioRepo.findById(guardada.getId()).orElseThrow();
        Assertions.assertEquals(2200000.0, buscada.getValor());
        Assertions.assertEquals("Sofia Ramirez", buscada.getUsuario().getNombre());
        Assertions.assertEquals(2200000.0, subastaUsuarioRepo.pujaMasAlta(1L));
    }

    @Test
    public void eliminarSubastaUsuarioTest() {
        subastaUsuarioRepo.deleteById(1L);
        sincronizar();

        Assertions.assertTrue(subastaUsuarioRepo.findById(1L).isEmpty());
        Assertions.assertEquals(3, subastaUsuarioRepo.count());
    }

    @Test
    public void actualizarSubastaUsuarioTest() {
        SubastaUsuario puja = subastaUsuarioRepo.findById(3L).orElseThrow();
        puja.setValor(255000.0);
        subastaUsuarioRepo.saveAndFlush(puja);
        sincronizar();

        Assertions.assertEquals(255000.0, subastaUsuarioRepo.findById(3L).orElseThrow().getValor());
    }

    @Test
    public void listarSubastaUsuarioTest() {
        List<SubastaUsuario> lista = subastaUsuarioRepo.findAll();
        Assertions.assertEquals(4, lista.size());
    }
}
