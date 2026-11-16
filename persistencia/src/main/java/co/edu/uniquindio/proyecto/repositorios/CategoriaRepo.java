package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Acceso a datos de {@link Categoria} (CRUD).
 */
@Repository
public interface CategoriaRepo extends JpaRepository<Categoria, Long> {
}
