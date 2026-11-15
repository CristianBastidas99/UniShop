package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.dto.CantidadDTO;
import co.edu.uniquindio.proyecto.dto.VentaMesDTO;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCompraRepo extends JpaRepository<DetalleCompra, Long> {

    /** Detalles (productos comprados) de una compra. */
    @Query("select d from DetalleCompra d where d.compra.id = :idCompra")
    List<DetalleCompra> listarPorCompra(@Param("idCompra") Long idCompra);

    /**
     * Reporte 2: dinero vendido por mes (unidades x precio guardado en cada detalle), en orden cronológico.
     * No descuenta los cupones; eso se ajusta cuando exista la lógica de compras (Paso 2.7).
     */
    @Query("select new co.edu.uniquindio.proyecto.dto.VentaMesDTO(year(c.fechaCompra), month(c.fechaCompra), " +
            "sum(d.unidades * d.precioProducto)) " +
            "from DetalleCompra d join d.compra c " +
            "group by year(c.fechaCompra), month(c.fechaCompra) " +
            "order by year(c.fechaCompra), month(c.fechaCompra)")
    List<VentaMesDTO> ventasPorMes();

    /** Reporte 3: vendedores con más unidades vendidas (usar {@code PageRequest.of(0, 5)} para el top 5). */
    @Query("select new co.edu.uniquindio.proyecto.dto.CantidadDTO(u.nombre, sum(d.unidades)) " +
            "from DetalleCompra d join d.producto p join p.usuario u " +
            "group by u.id, u.nombre order by sum(d.unidades) desc, u.nombre")
    List<CantidadDTO> topVendedores(Pageable pageable);
}
