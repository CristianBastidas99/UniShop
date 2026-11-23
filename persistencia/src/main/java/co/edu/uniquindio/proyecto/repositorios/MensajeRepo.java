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

    /**
     * Mensajes más recientes de varios chats en una sola consulta: los de la fecha máxima de cada chat
     * (si hay empate de fecha pueden venir varios por chat; quien llama se queda con el de mayor id).
     */
    @Query("select m from Mensaje m where m.chat.id in :idsChat and m.fecha = "
            + "(select max(m2.fecha) from Mensaje m2 where m2.chat.id = m.chat.id)")
    List<Mensaje> listarUltimosPorChats(@Param("idsChat") java.util.Collection<Long> idsChat);

    /** Indica si el usuario ha enviado al menos un mensaje. */
    boolean existsByRemitenteId(Long idUsuario);
}
