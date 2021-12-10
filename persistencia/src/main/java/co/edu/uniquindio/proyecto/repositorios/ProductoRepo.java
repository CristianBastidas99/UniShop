package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepo extends JpaRepository<Producto, Long> {

    @Query("select p from Producto p inner join Categoria c where c.nombre = :nombreCategoria")
    public List<Producto> FindAllWithDescriptionQuery(String nombreCategoria);

    @Query("select p from Producto p inner join Usuario u where u.email = :email")
    public List<Producto> FindAllWithDescriptionQueryFavoritos(String email);

}
