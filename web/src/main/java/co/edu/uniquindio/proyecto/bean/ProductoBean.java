package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@ViewScoped
public class ProductoBean implements Serializable {

    private final ProductoServicio productoServicio;

    private Producto producto;

    private String fecha;

    private List<Producto> listaProductos;

    @Value("${upload.url}")
    private String urlImagenes;

    private ArrayList<String> imagenes;

    public ProductoBean(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @PostConstruct
    public void inicializar() {
        this.producto = new Producto();
        this.fecha = " ";
        this.listaProductos = productoServicio.listarProducto();
        this.imagenes = new ArrayList<>();
    }

    public void registrarProducto() {
        try {
            producto.setFecha_limite(convertir(fecha));
            List<Producto> lista = productoServicio.listarProducto();
            producto.setId((long) (1 + lista.size()));
            productoServicio.registrarProducto(producto);
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Alerta",
                    "Se ha guardado correctamente");
            FacesContext.getCurrentInstance().addMessage(null, facesMsg);

        } catch (Exception e) {
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Alerta",
                    e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, facesMsg);

        }
    }

    public Date convertir(String fecha) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(fecha);
        return date;
    }

    public void subirImagenes(FileUploadEvent event) {
        UploadedFile imagen = event.getFile();
        String nombreImagen = subirImagen(imagen);
        if (nombreImagen != null) {
            imagenes.add(nombreImagen);
        }
    }

    private String subirImagen(UploadedFile imagen) {
        return "";
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }
}
