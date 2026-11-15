package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.repositorios.CuponRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class CuponTest extends CrudTestBase {

    private static final long UN_DIA = 24L * 60 * 60 * 1000;

    @Autowired
    private CuponRepo cuponRepo;

    @Test
    public void registrarCuponTest() {
        Cupon c = new Cupon();
        c.setCodigo("BLACKFRIDAY30");
        c.setPorcentaje(0.3);
        c.setFechaVencimiento(new Date(System.currentTimeMillis() + 7 * UN_DIA));
        c.setUsosMaximos(200);
        c.setUsosRealizados(0);

        Cupon guardado = cuponRepo.saveAndFlush(c);
        sincronizar();

        Cupon buscado = cuponRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals("BLACKFRIDAY30", buscado.getCodigo());
        Assertions.assertEquals(0.3, buscado.getPorcentaje());
        Assertions.assertEquals(200, buscado.getUsosMaximos());
    }

    @Test
    public void eliminarCuponTest() {
        // NAVIDAD20 (2) no se ha usado en ninguna compra
        cuponRepo.deleteById(2L);
        sincronizar();

        Assertions.assertTrue(cuponRepo.findById(2L).isEmpty());
        Assertions.assertEquals(2, cuponRepo.count());
    }

    @Test
    public void actualizarCuponTest() {
        // Se registra un uso del cupón BIENVENIDA10
        Cupon c = cuponRepo.findByCodigo("BIENVENIDA10").orElseThrow();
        c.setUsosRealizados(c.getUsosRealizados() + 1);
        cuponRepo.saveAndFlush(c);
        sincronizar();

        Assertions.assertEquals(3, cuponRepo.findByCodigo("BIENVENIDA10").orElseThrow().getUsosRealizados());
    }

    @Test
    public void listarCuponTest() {
        Set<String> codigos = cuponRepo.findAll().stream().map(Cupon::getCodigo).collect(Collectors.toSet());
        Assertions.assertEquals(Set.of("BIENVENIDA10", "NAVIDAD20", "VERANO15"), codigos);
    }
}
