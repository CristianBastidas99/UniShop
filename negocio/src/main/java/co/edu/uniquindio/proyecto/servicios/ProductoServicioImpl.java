package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServicioImpl implements ProductoServicio{

    private final ProductoRepo productoRepo;

    public ProductoServicioImpl(ProductoRepo productoRepo) {
        this.productoRepo = productoRepo;
    }

    @Override
    public Producto registrarProducto(Producto p) throws Exception {
        Optional<Producto> buscado = productoRepo.findById(p.getId());
        if (buscado.isPresent()) {
            throw new Exception("El producto ya se encuentra registrado");
        }
        return productoRepo.save(p);
    }

    @Override
    public Producto actualizarProducto(Producto p) throws Exception {
        Optional<Producto> buscado = productoRepo.findById(p.getId());
        if (!buscado.isPresent()) {
            throw new Exception("Se produjo un error y el producto no se encontro");
        }
        return productoRepo.save(p);
    }

    @Override
    public Producto obtenerProducto(Long id) throws Exception {
        Optional<Producto> buscado = productoRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("Un producto con ese id no existe");
        }
        return buscado.get();
    }

    @Override
    public void eliminarProducto(Long id) throws Exception {
        Optional<Producto> buscado = productoRepo.findById(id);
        if (!buscado.isPresent()) {
            throw new Exception("El producto que quiere eliminar no existe en la base de datos");
        }
        productoRepo.delete(buscado.get());
    }


    @Override
    public List<Producto> listarProducto() {
        return productoRepo.findAll();
    }

    @Override
    public List<Producto> listarProductoCategoria(String nombreCategoria) {
        return productoRepo.FindAllWithDescriptionQuery(nombreCategoria);
    }

    @Override
    public List<Producto> listarProductoFavoritos(String email) {
        return productoRepo.FindAllWithDescriptionQueryFavoritos(email);
    }
}
