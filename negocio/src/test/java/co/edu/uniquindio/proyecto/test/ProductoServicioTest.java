package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductoServicioTest extends BaseServicioTest {

    @Autowired
    private ProductoServicio productoServicio;
    @Autowired
    private UsuarioServicio usuarioServicio;

    private Producto nuevoProducto() throws NegocioException {
        Producto p = new Producto();
        p.setNombre("Teclado");
        p.setUnidades(5);
        p.setPrecio(50000.0);
        p.setFechaLimite(new Date(System.currentTimeMillis() + 86_400_000L));
        p.setDescuento(0.0);
        p.setUsuario(usuarioServicio.obtenerUsuario("lorena@email.com"));
        List<String> imagenes = new ArrayList<>();
        imagenes.add("teclado.png");
        p.setMisImagenes(imagenes);
        return p;
    }

    @Test
    public void registrarProductoTest() throws Exception {
        Producto guardado = productoServicio.registrarProducto(nuevoProducto());
        sincronizar();
        assertNotNull(guardado.getId());
        assertEquals("Teclado", productoServicio.obtenerProducto(guardado.getId()).getNombre());
    }

    @Test
    public void actualizarConIdNuloTest() throws Exception {
        Producto p = nuevoProducto();
        assertThrows(NegocioException.class, () -> productoServicio.actualizarProducto(p));
    }

    @Test
    public void obtenerProductoInexistenteTest() {
        assertThrows(NegocioException.class, () -> productoServicio.obtenerProducto(99999L));
    }
}
