package co.edu.uniquindio.proyecto.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Respuesta de la API con los datos de un producto. Nunca se devuelve la entidad (tiene relaciones cíclicas).
 * La fecha límite usa el formato {@code yyyy-MM-dd'T'HH:mm:ss} en zona horaria America/Bogota.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private int unidades;
    private Double precio;
    /** Descuento entre 0 y 1 (0 si no tiene). */
    private Double descuento;
    private Double precioConDescuento;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Bogota")
    private Date fechaLimite;
    private Long idVendedor;
    private String nombreVendedor;
    private Long idCiudad;
    private String nombreCiudad;
    private List<String> imagenes;
    private List<CategoriaDTO> categorias;
    /** Calificación promedio (0 si no tiene comentarios). */
    private Double calificacion;
}
