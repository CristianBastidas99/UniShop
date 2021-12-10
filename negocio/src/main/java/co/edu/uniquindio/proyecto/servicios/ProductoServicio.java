package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;

import java.util.List;

public interface ProductoServicio {

    Producto registrarProducto(Producto p) throws Exception;
    Producto actualizarProducto(Producto p) throws Exception;
    Producto obtenerProducto(Long id) throws Exception;
    void eliminarProducto(Long id) throws Exception;
    List<Producto> listarProducto();
    List<Producto> listarProductoCategoria(String nombreCategoria);
    List<Producto> listarProductoFavoritos(String email);

}
