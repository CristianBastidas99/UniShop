package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de {@link Comentario}: CRUD, comentarios por producto o usuario y calificación promedio.
 */
@Repository
public interface ComentarioRepo extends JpaRepository<Comentario, Long> {

    /** Indica si el producto tiene al menos un comentario. */
    boolean existsByProductoId(Long idProducto);

    /** Comentarios de un producto, del más reciente al más antiguo. */
    @Query("select c from Comentario c where c.producto.id = :idProducto order by c.fecha desc")
    List<Comentario> listarPorProducto(@Param("idProducto") Long idProducto);

    /** Comentarios escritos por un usuario. */
    @Query("select c from Comentario c where c.usuario.id = :idUsuario order by c.fecha desc")
    List<Comentario> listarPorUsuario(@Param("idUsuario") Long idUsuario);

    /** Calificación promedio de un producto, o {@code null} si no tiene comentarios. */
    @Query("select avg(c.calificacion) from Comentario c where c.producto.id = :idProducto")
    Double calificacionPromedio(@Param("idProducto") Long idProducto);
}
