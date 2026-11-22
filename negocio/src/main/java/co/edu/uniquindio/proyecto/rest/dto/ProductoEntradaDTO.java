package co.edu.uniquindio.proyecto.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import java.util.Date;
import java.util.List;

/**
 * Cuerpo de las peticiones POST y PUT de productos, con validaciones (mensajes en español).
 * {@code idVendedor} solo se usa al crear; al actualizar se ignora (el usuario actúa por {@code idUsuario}).
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductoEntradaDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
    private String descripcion;

    @NotNull(message = "Las unidades son obligatorias")
    @PositiveOrZero(message = "Las unidades no pueden ser negativas")
    private Integer unidades;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que cero")
    private Double precio;

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "1.0", message = "El descuento no puede ser mayor que 1")
    private Double descuento;

    @NotNull(message = "La fecha límite es obligatoria")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Bogota")
    private Date fechaLimite;

    private Long idVendedor;

    @NotNull(message = "La ciudad es obligatoria")
    private Long idCiudad;

    @NotEmpty(message = "Debe indicar al menos una imagen")
    private List<String> imagenes;

    @NotEmpty(message = "Debe indicar al menos una categoría")
    private List<Long> idsCategorias;
}
