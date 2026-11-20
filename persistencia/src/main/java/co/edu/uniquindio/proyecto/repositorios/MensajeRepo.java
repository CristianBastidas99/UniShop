package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de {@link Mensaje}: CRUD y mensajes de un chat ordenados por fecha.
 */
@Repository
public interface MensajeRepo extends JpaRepository<Mensaje, Long> {

    /** Mensajes de un chat, del más antiguo al más reciente. */
    @Query("select m from Mensaje m where m.chat.id = :idChat order by m.fecha asc")
    List<Mensaje> listarPorChat(@Param("idChat") Long idChat);

    /** Indica si el usuario ha enviado al menos un mensaje. */
    boolean existsByRemitenteId(Long idUsuario);
}
