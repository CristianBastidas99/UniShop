package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.repositorios.CiudadRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductoTest extends CrudTestBase {

    private static final long UN_DIA = 24L * 60 * 60 * 1000;

    @Autowired
    private ProductoRepo productoRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private CiudadRepo ciudadRepo;

    private Producto nuevoProducto() {
        Producto p = new Producto();
        p.setNombre("Audifonos Bluetooth");
        p.setDescripcion("Audifonos inalambricos con cancelacion de ruido");
        p.setUnidades(15);
        p.setPrecio(120000.0);
        p.setDescuento(0.15);
        p.setFechaLimite(new Date(System.currentTimeMillis() + 30 * UN_DIA));
        p.setUsuario(usuarioRepo.findById(7L).orElseThrow());
        p.setCiudad(ciudadRepo.findById(2L).orElseThrow());
        p.setMisImagenes(new ArrayList<>(List.of("audifonos.png", "audifonos-2.png")));
        return p;
    }

    @Test
    public void registrarProductoTest() {
        Producto guardado = productoRepo.saveAndFlush(nuevoProducto());
        sincronizar();

        Producto buscado = productoRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals("Audifonos Bluetooth", buscado.getNombre());
        Assertions.assertEquals(120000.0, buscado.getPrecio());
        Assertions.assertEquals("Pepe Perez", buscado.getUsuario().getNombre());
        Assertions.assertEquals("Medellin", buscado.getCiudad().getNombre());
        Assertions.assertEquals(2, buscado.getMisImagenes().size());
    }

    @Test
    public void eliminarProductoTest() {
        // Los productos del SQL tienen compras, chats o subastas, así que se elimina uno nuevo
        Producto guardado = productoRepo.saveAndFlush(nuevoProducto());
        productoRepo.delete(guardado);
        sincronizar();

        Assertions.assertTrue(productoRepo.findById(guardado.getId()).isEmpty());
        Assertions.assertEquals(6, productoRepo.count());
    }

    @Test
    public void actualizarProductoTest() {
        Producto p = productoRepo.findById(1L).orElseThrow();
        p.setPrecio(28000.0);
        p.setUnidades(18);
        p.setDescuento(0.0);
        productoRepo.saveAndFlush(p);
        sincronizar();

        Producto buscado = productoRepo.findById(1L).orElseThrow();
        Assertions.assertEquals(28000.0, buscado.getPrecio());
        Assertions.assertEquals(18, buscado.getUnidades());
        Assertions.assertEquals(0.0, buscado.getDescuento());
    }

    @Test
    public void listarProductoTest() {
        List<Producto> lista = productoRepo.findAll();
        Assertions.assertEquals(6, lista.size());
        Producto smartphone = productoRepo.findById(2L).orElseThrow();
        Assertions.assertEquals("SmartPhone", smartphone.getNombre());
        Assertions.assertEquals(2, smartphone.getMisImagenes().size());
        Assertions.assertEquals("Tecnologia", smartphone.getMisCategorias().get(0).getNombre());
    }
}
