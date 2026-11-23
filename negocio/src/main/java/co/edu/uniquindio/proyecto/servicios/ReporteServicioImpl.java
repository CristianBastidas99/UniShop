package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.CantidadDTO;
import co.edu.uniquindio.proyecto.dto.MedioPagoDTO;
import co.edu.uniquindio.proyecto.dto.PromedioDTO;
import co.edu.uniquindio.proyecto.dto.VentaMesDTO;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import co.edu.uniquindio.proyecto.repositorios.DetalleCompraRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de {@link ReporteServicio}: delega en las consultas de los repositorios y valida los límites.
 */
@Service
@Transactional(readOnly = true)
public class ReporteServicioImpl implements ReporteServicio {

    private static final int LIMITE_MINIMO = 1;
    private static final int LIMITE_MAXIMO = 50;

    private final ProductoRepo productoRepo;
    private final DetalleCompraRepo detalleCompraRepo;
    private final CompraRepo compraRepo;

    public ReporteServicioImpl(ProductoRepo productoRepo, DetalleCompraRepo detalleCompraRepo,
                               CompraRepo compraRepo) {
        this.productoRepo = productoRepo;
        this.detalleCompraRepo = detalleCompraRepo;
        this.compraRepo = compraRepo;
    }

    @Override
    public List<CantidadDTO> productosPorCategoria() {
        return productoRepo.contarPorCategoria();
    }

    @Override
    public List<VentaMesDTO> ventasPorMes() {
        return detalleCompraRepo.ventasPorMes();
    }

    @Override
    public List<CantidadDTO> topVendedores(int limite) throws NegocioException {
        validarLimite(limite);
        return detalleCompraRepo.topVendedores(PageRequest.of(0, limite));
    }

    @Override
    public List<PromedioDTO> mejorCalificados(int limite) throws NegocioException {
        validarLimite(limite);
        List<PromedioDTO> redondeados = new ArrayList<>();
        for (PromedioDTO p : productoRepo.mejorCalificados(PageRequest.of(0, limite))) {
            Double promedio = p.getPromedio() == null ? null
                    : BigDecimal.valueOf(p.getPromedio()).setScale(1, RoundingMode.HALF_UP).doubleValue();
            redondeados.add(new PromedioDTO(p.getNombre(), promedio));
        }
        return redondeados;
    }

    @Override
    public List<MedioPagoDTO> comprasPorMedioPago() {
        return compraRepo.contarPorMedioPago();
    }

    private void validarLimite(int limite) throws NegocioException {
        if (limite < LIMITE_MINIMO || limite > LIMITE_MAXIMO) {
            throw new NegocioException("El límite debe estar entre 1 y 50");
        }
    }
}
