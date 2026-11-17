package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.entidades.Categoria;
import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.servicios.CategoriaServicio;
import co.edu.uniquindio.proyecto.servicios.CiudadServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga las ciudades y categorías iniciales al arrancar, solo si las tablas están vacías.
 */
@Component
public class DatosInicialesConfig implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatosInicialesConfig.class);

    private static final List<String> CIUDADES = List.of("Armenia", "Bogotá", "Medellín", "Cali", "Pereira",
            "Manizales", "Barranquilla", "Cartagena", "Bucaramanga");
    private static final List<String> CATEGORIAS = List.of("Tecnología", "Hogar", "Ropa", "Deportes", "Libros",
            "Juguetes", "Salud y belleza");

    private final CiudadServicio ciudadServicio;
    private final CategoriaServicio categoriaServicio;

    public DatosInicialesConfig(CiudadServicio ciudadServicio, CategoriaServicio categoriaServicio) {
        this.ciudadServicio = ciudadServicio;
        this.categoriaServicio = categoriaServicio;
    }

    @Override
    public void run(String... args) throws Exception {
        if (ciudadServicio.listarCiudades().isEmpty()) {
            for (String nombre : CIUDADES) {
                Ciudad c = new Ciudad();
                c.setNombre(nombre);
                ciudadServicio.registrarCiudad(c);
            }
            LOG.info("Se cargaron {} ciudades iniciales", CIUDADES.size());
        }
        if (categoriaServicio.listarCategoria().isEmpty()) {
            for (String nombre : CATEGORIAS) {
                Categoria c = new Categoria();
                c.setNombre(nombre);
                categoriaServicio.registrarCategoria(c);
            }
            LOG.info("Se cargaron {} categorías iniciales", CATEGORIAS.size());
        }
    }
}
