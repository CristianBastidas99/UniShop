package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

/**
 * Acceso a datos de {@link Cupon}: CRUD, búsqueda por código y cupones disponibles.
 */
@Repository
public interface CuponRepo extends JpaRepository<Cupon, Long> {

    Optional<Cupon> findByCodigo(String codigo);

    /** Cupón con ese código que se puede usar hoy: no está vencido y le quedan usos. */
    @Query("select c from Cupon c where c.codigo = :codigo and c.fechaVencimiento > :hoy " +
            "and c.usosRealizados < c.usosMaximos")
    Optional<Cupon> buscarDisponible(@Param("codigo") String codigo, @Param("hoy") Date hoy);
}
