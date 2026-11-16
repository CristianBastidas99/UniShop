package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de {@link SubastaUsuario} (pujas): CRUD, puja más alta y pujas de una subasta.
 */
@Repository
public interface SubastaUsuarioRepo extends JpaRepository<SubastaUsuario, Long> {

    /** Indica si la subasta tiene al menos una puja. */
    boolean existsBySubastaId(Long idSubasta);

    /** Valor de la puja más alta de una subasta, o {@code null} si nadie ha pujado. */
    @Query("select max(s.valor) from SubastaUsuario s where s.subasta.id = :idSubasta")
    Double pujaMasAlta(@Param("idSubasta") Long idSubasta);

    /** Pujas de una subasta de la mayor a la menor (la primera es la ganadora). */
    @Query("select s from SubastaUsuario s where s.subasta.id = :idSubasta order by s.valor desc, s.fechaSubasta asc")
    List<SubastaUsuario> listarPorSubasta(@Param("idSubasta") Long idSubasta);
}
