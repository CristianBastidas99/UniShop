package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acceso a datos de {@link Ciudad} (CRUD).
 */
@Repository
public interface CiudadRepo extends JpaRepository<Ciudad, Long> {

    /** Ciudad con ese nombre exacto, si existe. */
    Optional<Ciudad> findByNombre(String nombre);

}
