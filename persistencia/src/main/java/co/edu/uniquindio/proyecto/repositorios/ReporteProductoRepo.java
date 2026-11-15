package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.entidades.ReporteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteProductoRepo extends JpaRepository<ReporteProducto, Long> {

    /** Reportes en un estado (por ejemplo, los PENDIENTE que debe revisar el administrador), los más antiguos primero. */
    @Query("select r from ReporteProducto r where r.estado = :estado order by r.fecha asc")
    List<ReporteProducto> listarPorEstado(@Param("estado") EstadoReporte estado);

    /** Reportes recibidos por un producto. */
    @Query("select r from ReporteProducto r where r.producto.id = :idProducto order by r.fecha desc")
    List<ReporteProducto> listarPorProducto(@Param("idProducto") Long idProducto);
}
