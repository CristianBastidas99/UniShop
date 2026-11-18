package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.ProductoServicio;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@ViewScoped
public class ProductoBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ProductoBean.class);

    /** Ids del formulario, su botón y el cargador de imágenes (deben coincidir con registraProducto.xhtml). */
    private static final String ID_FORMULARIO = "formProducto";
    private static final String ID_BOTON = ID_FORMULARIO + ":botonRegistrar";
    private static final String ID_IMAGENES = ID_FORMULARIO + ":imagenes_producto";

    /** Extensiones de imagen aceptadas al subir archivos. */
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of(".jpg", ".jpeg", ".png", ".gif");

    private final ProductoServicio productoServicio;
    private final SeguridadBean seguridadBean;

    private Producto producto;

    private List<Producto> listaProductos;

    @Value("${upload.url}")
    private String urlImagenes;

    private ArrayList<String> imagenes;

    private List<Categoria> categoriasSeleccionadas;

    public ProductoBean(ProductoServicio productoServicio, SeguridadBean seguridadBean) {
        this.productoServicio = productoServicio;
        this.seguridadBean = seguridadBean;
    }

    @PostConstruct
    public void inicializar() {
        this.producto = new Producto();
        this.listaProductos = productoServicio.listarVigentes();
        this.imagenes = new ArrayList<>();
        this.categoriasSeleccionadas = new ArrayList<>();
    }

    /** Publica el producto del usuario en sesión. Devuelve la navegación al inicio, o null si hay errores. */
    public String registrarProducto() {
        FacesContext contexto = FacesContext.getCurrentInstance();
        Long idUsuario = seguridadBean.getIdUsuario();
        if (idUsuario == null) {
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Debes iniciar sesión como usuario para publicar", null));
            return null;
        }
        try {
            // El servicio recarga el vendedor desde la base de datos a partir de su id
            Usuario vendedor = new Usuario();
            vendedor.setId(idUsuario);
            producto.setUsuario(vendedor);
            producto.setMisImagenes(imagenes);
            List<Long> idsCategorias = new ArrayList<>();
            for (Categoria categoria : categoriasSeleccionadas) {
                idsCategorias.add(categoria.getId());
            }
            productoServicio.publicarProducto(producto, idsCategorias);
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Producto publicado", "Tu producto se publicó correctamente."));
            contexto.getExternalContext().getFlash().setKeepMessages(true);
            return "/index?faces-redirect=true";
        } catch (NegocioException e) {
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo publicar el producto", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Error inesperado al publicar el producto", e);
            contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo publicar el producto", "Ocurrió un error inesperado. Inténtalo de nuevo más tarde."));
        }
        return null;
    }

    /**
     * Se ejecuta al terminar la validación del formulario (f:event postValidate). Como las imágenes se suben
     * aparte, se comprueba aquí que haya al menos una, para mostrar ese error junto al de los demás campos.
     */
    public void validarImagenes(ComponentSystemEvent evento) {
        FacesContext contexto = FacesContext.getCurrentInstance();
        // Solo al enviar el formulario con el botón Publicar (no en las peticiones de subida de archivos)
        if (!contexto.getExternalContext().getRequestParameterMap().containsKey(ID_BOTON)) {
            return;
        }
        if (imagenes.isEmpty()) {
            contexto.addMessage(ID_IMAGENES, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Sube al menos una imagen del producto", "Sube al menos una imagen del producto"));
            contexto.validationFailed();
            contexto.renderResponse();
        }
    }

    /** Primer día en que puede vencer la publicación: mañana. */
    public Date getManana() {
        return Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Valida que la fecha límite sea futura (a partir de mañana). */
    public void validarFechaLimite(FacesContext contexto, UIComponent componente, Object valor) {
        if (valor instanceof Date) {
            LocalDate fecha = ((Date) valor).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (!fecha.isAfter(LocalDate.now())) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "La fecha límite debe ser posterior a hoy", "La fecha límite debe ser posterior a hoy"));
            }
        }
    }

    public void subirImagenes(FileUploadEvent event) {
        UploadedFile imagen = event.getFile();
        String nombreImagen = subirImagen(imagen);
        if (nombreImagen != null) {
            imagenes.add(nombreImagen);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Imagen subida", imagen.getFileName()));
        }
    }

    /** Copia la imagen a la carpeta de subidas con un nombre único y devuelve su ruta, o null si falla. */
    private String subirImagen(UploadedFile imagen) {
        String original = imagen.getFileName() == null ? "" : imagen.getFileName();
        int punto = original.lastIndexOf('.');
        String extension = punto >= 0 ? original.substring(punto).toLowerCase(Locale.ROOT) : "";
        // El allowTypes del fileUpload solo se revisa en el navegador: aquí se valida en el servidor
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Archivo no permitido", "Solo se permiten imágenes JPG, PNG o GIF."));
            return null;
        }
        // Nombre generado: evita sobrescribir imágenes y rutas manipuladas en el nombre original
        String nombre = UUID.randomUUID() + extension;
        File archivo = new File(urlImagenes, nombre);
        try (InputStream entrada = imagen.getInputStream();
             OutputStream salida = Files.newOutputStream(archivo.toPath())) {
            IOUtils.copy(entrada, salida);
            return "uploads/" + nombre;
        } catch (IOException e) {
            LOG.error("No se pudo guardar la imagen {}", original, e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo subir la imagen", "No se pudo subir " + original + ". Inténtalo de nuevo."));
            return null;
        }
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public List<Categoria> getCategoriasSeleccionadas() {
        return categoriasSeleccionadas;
    }

    public void setCategoriasSeleccionadas(List<Categoria> categoriasSeleccionadas) {
        this.categoriasSeleccionadas = categoriasSeleccionadas;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }
}
