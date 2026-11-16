package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Acceso a datos de {@link Ciudad} (CRUD).
 */
@Repository
public interface CiudadRepo extends JpaRepository<Ciudad, Long> {



}
