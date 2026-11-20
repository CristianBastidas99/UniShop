package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    /** Indica si el producto tiene al menos un reporte. */
    boolean existsByProductoId(Long idProducto);

    /** Reportes recibidos por un producto. */
    @Query("select r from ReporteProducto r where r.producto.id = :idProducto order by r.fecha desc")
    List<ReporteProducto> listarPorProducto(@Param("idProducto") Long idProducto);

    /** Indica si el usuario ha hecho al menos un reporte. */
    boolean existsByUsuarioId(Long idUsuario);

    /** Reportes en alguno de los estados indicados, los más antiguos primero. */
    @Query("select r from ReporteProducto r where r.estado in :estados order by r.fecha asc, r.id asc")
    List<ReporteProducto> listarPorEstadosAntiguosPrimero(@Param("estados") Collection<EstadoReporte> estados);

    /** Reportes en alguno de los estados indicados, los más recientes primero. */
    @Query("select r from ReporteProducto r where r.estado in :estados order by r.fecha desc, r.id desc")
    List<ReporteProducto> listarPorEstadosRecientesPrimero(@Param("estados") Collection<EstadoReporte> estados);
}
