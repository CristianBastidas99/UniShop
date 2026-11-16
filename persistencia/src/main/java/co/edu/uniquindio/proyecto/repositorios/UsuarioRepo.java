package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Usuario}: CRUD, búsqueda por email y login.
 */
@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    /** Usuarios que tienen el producto en su lista de favoritos. */
    @Query("select u from Usuario u join u.misProductosFavoritos f where f.id = :idProducto")
    List<Usuario> listarConFavorito(@Param("idProducto") Long idProducto);

    /** Indica si hay al menos un usuario que vive en esa ciudad. */
    boolean existsByCiudadId(Long idCiudad);

    /** Usuario con ese email y contraseña (login). */
    @Query("select u from Usuario u where u.email = :email and u.password = :password")
    Optional<Usuario> buscarPorEmailYPassword(@Param("email") String email, @Param("password") String password);
}
