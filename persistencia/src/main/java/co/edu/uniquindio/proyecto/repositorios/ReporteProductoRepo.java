package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de {@link ReporteProducto}: CRUD y reportes por estado o por producto.
 */
@Repository
public interface ReporteProductoRepo extends JpaRepository<ReporteProducto, Long> {

    /** Reportes en un estado (por ejemplo, los PENDIENTE que debe revisar el administrador), los más antiguos primero. */
    @Query("select r from ReporteProducto r where r.estado = :estado order by r.fecha asc")
    List<ReporteProducto> listarPorEstado(@Param("estado") EstadoReporte estado);

    /** Indica si un usuario ya reportó un producto (un usuario solo puede reportar una vez el mismo producto). */
    boolean existsByUsuarioIdAndProductoId(Long idUsuario, Long idProducto);

    /** Reportes recibidos por un producto. */
    @Query("select r from ReporteProducto r where r.producto.id = :idProducto order by r.fecha desc")
    List<ReporteProducto> listarPorProducto(@Param("idProducto") Long idProducto);
}
