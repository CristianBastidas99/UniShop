package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Acceso a datos de {@link Subasta}: CRUD y subastas activas.
 */
@Repository
public interface SubastaRepo extends JpaRepository<Subasta, Long> {

    /** Indica si el producto tiene al menos una subasta (activa o terminada). */
    boolean existsByProductoId(Long idProducto);

    /** Subastas activas (fecha límite posterior a {@code hoy}), las que terminan primero van primero. */
    @Query("select s from Subasta s where s.fechaLimite > :hoy order by s.fechaLimite asc")
    List<Subasta> listarActivas(@Param("hoy") Date hoy);
}
