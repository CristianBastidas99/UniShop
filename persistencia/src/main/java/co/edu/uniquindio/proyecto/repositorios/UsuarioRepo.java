package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Usuario}: CRUD y búsqueda por email (el login compara la contraseña encriptada en el servicio).
 */
@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    /** Usuarios que tienen el producto en su lista de favoritos. */
    @Query("select u from Usuario u join u.misProductosFavoritos f where f.id = :idProducto")
    List<Usuario> listarConFavorito(@Param("idProducto") Long idProducto);

    /** Indica si hay al menos un usuario que vive en esa ciudad. */
    boolean existsByCiudadId(Long idCiudad);

    /** Indica si existe un usuario con ese id y su cuenta está activa (consulta liviana, sin cargar la entidad). */
    boolean existsByIdAndActivoTrue(Long id);

    /** Usuarios cuyo nombre o email contiene el texto (sin distinguir mayúsculas), ordenados por nombre. */
    @Query("select u from Usuario u where lower(u.nombre) like lower(concat('%', :texto, '%')) " +
            "or lower(u.email) like lower(concat('%', :texto, '%')) order by u.nombre, u.id")
    List<Usuario> buscarPorTexto(@Param("texto") String texto);

    /** Todos los usuarios ordenados por nombre. */
    List<Usuario> findAllByOrderByNombreAscIdAsc();
}
