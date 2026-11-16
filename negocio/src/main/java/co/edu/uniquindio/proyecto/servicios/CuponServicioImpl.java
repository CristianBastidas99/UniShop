package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import co.edu.uniquindio.proyecto.repositorios.CuponRepo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CuponServicioImpl implements CuponServicio {

    private final CuponRepo cuponRepo;
    private final CompraRepo compraRepo;

    public CuponServicioImpl(CuponRepo cuponRepo, CompraRepo compraRepo) {
        this.cuponRepo = cuponRepo;
        this.compraRepo = compraRepo;
    }

    @Override
    public Cupon registrarCupon(Cupon c) throws NegocioException {
        if (c.getId() != null && cuponRepo.existsById(c.getId())) {
            throw new NegocioException("El cupón ya existe");
        }
        if (cuponRepo.findByCodigo(c.getCodigo()).isPresent()) {
            throw new NegocioException("El cupón " + c.getCodigo() + " ya existe");
        }
        return cuponRepo.save(c);
    }

    @Override
    public Cupon actualizarCupon(Cupon c) throws NegocioException {
        if (c.getId() == null || !cuponRepo.existsById(c.getId())) {
            throw new NegocioException("El cupón no existe");
        }
        boolean codigoDeOtro = cuponRepo.findByCodigo(c.getCodigo())
                .map(otro -> !otro.getId().equals(c.getId()))
                .orElse(false);
        if (codigoDeOtro) {
            throw new NegocioException("El cupón " + c.getCodigo() + " ya existe");
        }
        return cuponRepo.save(c);
    }

    @Override
    public Cupon obtenerCupon(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("El cupón no existe");
        }
        return cuponRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El cupón no existe"));
    }

    @Override
    public void eliminarCupon(Long id) throws NegocioException {
        Cupon cupon = obtenerCupon(id);
        if (cupon.getUsosRealizados() > 0 || compraRepo.existsByCuponId(id)) {
            throw new NegocioException("El cupón ya se usó en compras y no se puede eliminar");
        }
        cuponRepo.delete(cupon);
    }

    @Override
    public List<Cupon> listarCupones() {
        return cuponRepo.findAll();
    }

    @Override
    public Cupon obtenerPorCodigo(String codigo) throws NegocioException {
        return cuponRepo.findByCodigo(codigo)
                .orElseThrow(() -> new NegocioException("El cupón " + codigo + " no existe"));
    }

    @Override
    public Cupon obtenerDisponible(String codigo) throws NegocioException {
        Cupon cupon = obtenerPorCodigo(codigo);
        if (!cupon.getFechaVencimiento().after(new Date())) {
            throw new NegocioException("El cupón " + codigo + " está vencido");
        }
        if (cupon.getUsosRealizados() >= cupon.getUsosMaximos()) {
            throw new NegocioException("El cupón " + codigo + " ya no tiene usos disponibles");
        }
        return cupon;
    }
}
