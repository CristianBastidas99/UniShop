package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Chat}: CRUD y chats por producto o por usuario (comprador o vendedor).
 */
@Repository
public interface ChatRepo extends JpaRepository<Chat, Long> {

    /** Indica si el producto tiene al menos un chat. */
    boolean existsByProductoId(Long idProducto);

    /** Chats abiertos sobre un producto. */
    @Query("select c from Chat c where c.producto.id = :idProducto")
    List<Chat> listarPorProducto(@Param("idProducto") Long idProducto);

    /** Chats en los que participa un usuario, como comprador o como vendedor del producto. */
    @Query("select c from Chat c where c.usuario.id = :idUsuario or c.producto.usuario.id = :idUsuario")
    List<Chat> listarPorUsuario(@Param("idUsuario") Long idUsuario);

    /** Chat de un comprador sobre un producto (para no crear dos chats iguales). */
    @Query("select c from Chat c where c.usuario.id = :idComprador and c.producto.id = :idProducto")
    Optional<Chat> buscarPorCompradorYProducto(@Param("idComprador") Long idComprador,
                                               @Param("idProducto") Long idProducto);
}
