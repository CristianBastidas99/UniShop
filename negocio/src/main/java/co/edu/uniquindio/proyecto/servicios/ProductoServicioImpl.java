package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ProductoServicioImpl implements ProductoServicio {

    private final ProductoRepo productoRepo;

    public ProductoServicioImpl(ProductoRepo productoRepo) {
        this.productoRepo = productoRepo;
    }

    @Override
    public Producto registrarProducto(Producto producto) throws NegocioException {
        if (producto.getId() != null && productoRepo.existsById(producto.getId())) {
            throw new NegocioException("El producto ya existe");
        }
        return productoRepo.save(producto);
    }

    @Override
    public Producto actualizarProducto(Producto producto) throws NegocioException {
        if (producto.getId() == null || !productoRepo.existsById(producto.getId())) {
            throw new NegocioException("El producto no existe");
        }
        return productoRepo.save(producto);
    }

    @Override
    public Producto obtenerProducto(Long id) throws NegocioException {
        return productoRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El producto no existe"));
    }

    @Override
    public void eliminarProducto(Long id) throws NegocioException {
        Producto buscado = obtenerProducto(id);
        productoRepo.delete(buscado);
    }

    @Override
    public List<Producto> listarProducto() {
        return productoRepo.findAll();
    }

    @Override
    public List<Producto> listarProductoCategoria(Long idCategoria) {
        return productoRepo.listarVigentesPorCategoria(idCategoria, new Date());
    }

    @Override
    public List<Producto> listarProductoBusqueda(String busqueda) {
        return productoRepo.buscar(busqueda, null, null, null, null, null, new Date());
    }

    @Override
    public List<Producto> listarProductoFavoritos(String email) {
        return productoRepo.listarFavoritos(email);
    }
}
