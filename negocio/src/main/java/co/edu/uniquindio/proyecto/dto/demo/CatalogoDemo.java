package co.edu.uniquindio.proyecto.dto.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Estructura del archivo {@code demo/catalogo.json} con los datos de demostración.
 * Los campos desconocidos del JSON (p. ej. {@code _descripcion} o {@code icono}) se ignoran.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogoDemo {

    /** Contraseña común de todas las cuentas de demostración. */
    private String password;
    private List<PersonaDemo> administradores = new ArrayList<>();
    private List<PersonaDemo> vendedores = new ArrayList<>();
    private List<PersonaDemo> compradores = new ArrayList<>();
    private List<ProductoDemo> productos = new ArrayList<>();

    /** Administrador, vendedor o comprador del catálogo. */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonaDemo {
        private String clave;
        private String nombre;
        private String email;
        private String ciudad;
        private List<String> telefonos = new ArrayList<>();
    }

    /** Producto del catálogo: la categoría, el vendedor (por clave) y la ciudad se indican por nombre. */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductoDemo {
        private String imagen;
        private String nombre;
        private String descripcion;
        private double precio;
        private int unidades;
        private Double descuento;
        private List<String> categorias = new ArrayList<>();
        private String vendedor;
        private String ciudad;
    }
}
