package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.List;

@Component
@ViewScoped
public class BusquedaBean implements Serializable {

    private final ProductoServicio productoServicio;

    @Getter
    @Setter
    @Value("#{param['busqueda']}")
    private String busquedaParam;

    @Getter
    @Setter
    private String busqueda;

    @Getter
    @Setter
    private List<Producto> productos;

    public BusquedaBean(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @PostConstruct
    public void inicializar() {
        if( busquedaParam!=null ) {
            productos = productoServicio.listarProductoBusqueda(busquedaParam);
        }
    }

    public String buscar() {
        return "resultadoBusqueda?faces-redirect=true&amp;busqueda="+busqueda;
    }

}
