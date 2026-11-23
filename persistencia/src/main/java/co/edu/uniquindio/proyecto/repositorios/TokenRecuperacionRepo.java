package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link TokenRecuperacion}: CRUD, búsqueda por hash y tokens vigentes de un correo.
 */
@Repository
public interface TokenRecuperacionRepo extends JpaRepository<TokenRecuperacion, Long> {

    /** Token con ese hash SHA-256 (hexadecimal). */
    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    /** Tokens de ese correo que todavía no se han usado ni invalidado (hay que marcarlos al pedir uno nuevo). */
    List<TokenRecuperacion> findByEmailAndUsadoFalse(String email);
}
