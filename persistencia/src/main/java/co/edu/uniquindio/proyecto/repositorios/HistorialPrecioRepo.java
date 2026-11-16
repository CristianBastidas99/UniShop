package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.HistorialPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de {@link HistorialPrecio}: CRUD e historial de un producto.
 */
@Repository
public interface HistorialPrecioRepo extends JpaRepository<HistorialPrecio, Long> {

    /** Cambios de precio de un producto, del más reciente al más antiguo. */
    @Query("select h from HistorialPrecio h where h.producto.id = :idProducto order by h.fecha desc")
    List<HistorialPrecio> listarPorProducto(@Param("idProducto") Long idProducto);
}
