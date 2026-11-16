package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.dto.MedioPagoDTO;
import co.edu.uniquindio.proyecto.entidades.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de {@link Compra}: CRUD, compras de un usuario y reporte de compras por medio de pago.
 */
@Repository
public interface CompraRepo extends JpaRepository<Compra, Long> {

    /** Compras de un usuario, de la más reciente a la más antigua. */
    @Query("select c from Compra c where c.usuario.id = :idUsuario order by c.fechaCompra desc")
    List<Compra> listarPorUsuario(@Param("idUsuario") Long idUsuario);

    /** Reporte 5: cantidad de compras por medio de pago. */
    @Query("select new co.edu.uniquindio.proyecto.dto.MedioPagoDTO(c.medioPago, count(c)) " +
            "from Compra c group by c.medioPago order by count(c) desc")
    List<MedioPagoDTO> contarPorMedioPago();
}
