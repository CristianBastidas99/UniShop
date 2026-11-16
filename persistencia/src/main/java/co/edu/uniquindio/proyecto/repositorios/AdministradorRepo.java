package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acceso a datos de {@link Administrador}: CRUD, búsqueda por email y login.
 */
@Repository
public interface AdministradorRepo extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByEmail(String email);

    /** Administrador con ese email y contraseña (login). */
    @Query("select a from Administrador a where a.email = :email and a.password = :password")
    Optional<Administrador> buscarPorEmailYPassword(@Param("email") String email, @Param("password") String password);
}
