package co.edu.uniquindio.proyecto.rest;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.rest.dto.CategoriaDTO;
import co.edu.uniquindio.proyecto.rest.dto.ProductoDTO;
import co.edu.uniquindio.proyecto.rest.dto.ProductoEntradaDTO;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte entre la entidad {@link Producto} y los DTO de la API REST. Debe usarse con la sesión de
 * persistencia abierta (open-in-view activo) porque lee imágenes y categorías (carga perezosa).
 */
@Component
public class ProductoMapper {

    @Autowired
    private ProductoServicio productoServicio;

    /** Convierte a DTO leyendo las categorías del producto (carga perezosa; para lecturas). */
    public ProductoDTO aDTO(Producto p, Double calificacion) {
        return aDTO(p, calificacion, p.getMisCategorias());
    }

    /**
     * Convierte a DTO con las categorías indicadas. Se usa tras escrituras, porque la colección
     * {@code misCategorias} de la entidad recién guardada puede estar desactualizada o nula.
     * La calificación se pasa ya calculada (puede ser null).
     */
    public ProductoDTO aDTO(Producto p, Double calificacion, List<Categoria> categorias) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setUnidades(p.getUnidades());
        dto.setPrecio(p.getPrecio());
        dto.setDescuento(p.getDescuento() == null ? 0.0 : p.getDescuento());
        dto.setPrecioConDescuento(productoServicio.calcularPrecioConDescuento(p));
        dto.setFechaLimite(p.getFechaLimite());
        if (p.getUsuario() != null) {
            dto.setIdVendedor(p.getUsuario().getId());
            dto.setNombreVendedor(p.getUsuario().getNombre());
        }
        if (p.getCiudad() != null) {
            dto.setIdCiudad(p.getCiudad().getId());
            dto.setNombreCiudad(p.getCiudad().getNombre());
        }
        dto.setImagenes(p.getMisImagenes() == null ? new ArrayList<>() : new ArrayList<>(p.getMisImagenes()));
        dto.setCategorias(categorias == null ? new ArrayList<>() : categorias.stream()
                .map(c -> new CategoriaDTO(c.getId(), c.getNombre())).collect(Collectors.toList()));
        dto.setCalificacion(calificacion == null ? 0.0 : calificacion);
        return dto;
    }

    /** Convierte la entrada a entidad (vendedor y ciudad solo con su id; el servicio los carga y valida). */
    public Producto aEntidad(ProductoEntradaDTO e) {
        Producto p = new Producto();
        p.setNombre(e.getNombre());
        p.setDescripcion(e.getDescripcion());
        p.setUnidades(e.getUnidades() == null ? 0 : e.getUnidades());
        p.setPrecio(e.getPrecio());
        p.setDescuento(e.getDescuento());
        p.setFechaLimite(e.getFechaLimite());
        p.setMisImagenes(e.getImagenes() == null ? null : new ArrayList<>(e.getImagenes()));
        if (e.getIdVendedor() != null) {
            Usuario u = new Usuario();
            u.setId(e.getIdVendedor());
            p.setUsuario(u);
        }
        if (e.getIdCiudad() != null) {
            Ciudad c = new Ciudad();
            c.setId(e.getIdCiudad());
            p.setCiudad(c);
        }
        return p;
    }
}
