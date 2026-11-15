package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SubastaRepo extends JpaRepository<Subasta, Long> {

    /** Subastas activas (fecha límite posterior a {@code hoy}), las que terminan primero van primero. */
    @Query("select s from Subasta s where s.fechaLimite > :hoy order by s.fechaLimite asc")
    List<Subasta> listarActivas(@Param("hoy") Date hoy);
}
