package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.MedioPago;
import co.edu.uniquindio.proyecto.servicios.CategoriaServicio;
import co.edu.uniquindio.proyecto.servicios.CiudadServicio;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

/** Listas para los componentes de selección (ciudades, categorías y medios de pago). */
@Component
@RequestScope
public class SeleccionBean {

    private final CiudadServicio ciudadServicio;
    private final CategoriaServicio categoriaServicio;
    private List<Ciudad> ciudades;
    private List<Categoria> categorias;

    public SeleccionBean(CiudadServicio ciudadServicio, CategoriaServicio categoriaServicio) {
        this.ciudadServicio = ciudadServicio;
        this.categoriaServicio = categoriaServicio;
    }

    public List<Ciudad> getCiudades() {
        if (ciudades == null) {
            ciudades = ciudadServicio.listarCiudades();
        }
        return ciudades;
    }

    public List<Categoria> getCategorias() {
        if (categorias == null) {
            categorias = categoriaServicio.listarCategoria();
        }
        return categorias;
    }

    public MedioPago[] getMediosPago() {
        return MedioPago.values();
    }

    /** Etiqueta legible de un medio de pago. */
    public String etiqueta(MedioPago medio) {
        switch (medio) {
            case TARJETA: return "Tarjeta";
            case PAYPAL: return "PayPal";
            case PSE: return "PSE";
            case BALOTO: return "Baloto";
            default: return medio.name();
        }
    }
}
