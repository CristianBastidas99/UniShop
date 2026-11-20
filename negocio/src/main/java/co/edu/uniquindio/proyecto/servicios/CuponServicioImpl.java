package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Cupon;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.CompraRepo;
import co.edu.uniquindio.proyecto.repositorios.CuponRepo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class CuponServicioImpl implements CuponServicio {

    private final CuponRepo cuponRepo;
    private final CompraRepo compraRepo;

    public CuponServicioImpl(CuponRepo cuponRepo, CompraRepo compraRepo) {
        this.cuponRepo = cuponRepo;
        this.compraRepo = compraRepo;
    }

    /** Código en mayúsculas y sin espacios en los extremos (null si viene nulo). */
    private static String normalizarCodigo(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase(Locale.ROOT);
    }

    /** Valida y normaliza el código y el porcentaje, comunes a registrar y actualizar. */
    private void validarCodigoYPorcentaje(Cupon c) throws NegocioException {
        c.setCodigo(normalizarCodigo(c.getCodigo()));
        if (c.getCodigo() == null || c.getCodigo().isEmpty()) {
            throw new NegocioException("El código del cupón es obligatorio");
        }
        Double porcentaje = c.getPorcentaje();
        if (porcentaje == null || porcentaje.isNaN() || porcentaje <= 0 || porcentaje >= 1) {
            throw new NegocioException("El porcentaje del cupón debe estar entre 1 % y 99 %");
        }
    }

    @Override
    public Cupon registrarCupon(Cupon c) throws NegocioException {
        if (c.getId() != null && cuponRepo.existsById(c.getId())) {
            throw new NegocioException("El cupón ya existe");
        }
        validarCodigoYPorcentaje(c);
        if (c.getFechaVencimiento() == null || !c.getFechaVencimiento().after(new Date())) {
            throw new NegocioException("La fecha de vencimiento es obligatoria y debe ser futura");
        }
        if (c.getUsosMaximos() < 1) {
            throw new NegocioException("Los usos máximos deben ser al menos 1");
        }
        if (cuponRepo.findByCodigo(c.getCodigo()).isPresent()) {
            throw new NegocioException("El cupón " + c.getCodigo() + " ya existe");
        }
        c.setUsosRealizados(0);
        return cuponRepo.save(c);
    }

    @Override
    public Cupon actualizarCupon(Cupon c) throws NegocioException {
        if (c.getId() == null) {
            throw new NegocioException("El cupón no existe");
        }
        Cupon existente = cuponRepo.findById(c.getId())
                .orElseThrow(() -> new NegocioException("El cupón no existe"));
        validarCodigoYPorcentaje(c);
        if (c.getFechaVencimiento() == null) {
            throw new NegocioException("La fecha de vencimiento es obligatoria");
        }
        if (c.getUsosMaximos() < existente.getUsosRealizados()) {
            throw new NegocioException("Los usos máximos no pueden ser menores que los usos ya realizados");
        }
        boolean codigoDeOtro = cuponRepo.findByCodigo(c.getCodigo())
                .map(otro -> !otro.getId().equals(c.getId()))
                .orElse(false);
        if (codigoDeOtro) {
            throw new NegocioException("El cupón " + c.getCodigo() + " ya existe");
        }
        // Los usos realizados no se editan desde fuera: se conservan los de la base.
        int usosRealizados = existente.getUsosRealizados();
        c.setUsosRealizados(usosRealizados);
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
        String normalizado = normalizarCodigo(codigo);
        return cuponRepo.findByCodigo(normalizado == null ? "" : normalizado)
                .orElseThrow(() -> new NegocioException("El cupón " + normalizado + " no existe"));
    }

    @Override
    public Cupon obtenerDisponible(String codigo) throws NegocioException {
        Cupon cupon = obtenerPorCodigo(codigo);
        if (!cupon.getFechaVencimiento().after(new Date())) {
            throw new NegocioException("El cupón " + cupon.getCodigo() + " está vencido");
        }
        if (cupon.getUsosRealizados() >= cupon.getUsosMaximos()) {
            throw new NegocioException("El cupón " + cupon.getCodigo() + " ya no tiene usos disponibles");
        }
        return cupon;
    }
}
