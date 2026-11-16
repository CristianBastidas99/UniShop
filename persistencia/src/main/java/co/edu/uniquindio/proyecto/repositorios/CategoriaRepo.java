package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Categoria} (CRUD).
 */
@Repository
public interface CategoriaRepo extends JpaRepository<Categoria, Long> {

    /** Categorías a las que pertenece un producto. */
    @Query("select c from Categoria c join c.misProductos p where p.id = :idProducto")
    List<Categoria> listarPorProducto(@Param("idProducto") Long idProducto);

    /** Categoría con ese nombre exacto, si existe. */
    Optional<Categoria> findByNombre(String nombre);
}
