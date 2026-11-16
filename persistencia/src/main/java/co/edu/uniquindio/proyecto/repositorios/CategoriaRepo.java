package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acceso a datos de {@link Categoria} (CRUD).
 */
@Repository
public interface CategoriaRepo extends JpaRepository<Categoria, Long> {

    /** Categoría con ese nombre exacto, si existe. */
    Optional<Categoria> findByNombre(String nombre);
}
