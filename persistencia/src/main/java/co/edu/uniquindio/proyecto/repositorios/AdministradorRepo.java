package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acceso a datos de {@link Administrador}: CRUD y búsqueda por email (el login compara la contraseña encriptada en el servicio).
 */
@Repository
public interface AdministradorRepo extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByEmail(String email);
}
