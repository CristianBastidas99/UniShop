package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.EstadoReporte;
import co.edu.uniquindio.proyecto.repositorios.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Verifica el archivo de datos de prueba unishop.sql (Paso 1.5): que corra sin errores sobre la base
 * que crea Hibernate, que todas las tablas queden con 3 filas o más y que los datos sean coherentes.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:unishop.sql")
public class DatosPruebaTest {

    /** Todas las tablas que crea Hibernate (menos hibernate_sequence). */
    private static final List<String> TABLAS = List.of(
            "administrador", "ciudad", "usuario", "telefono", "categoria", "producto", "imagen",
            "producto_categoria", "favorito", "comentario", "cupon", "compra", "detalle_compra",
            "chat", "mensaje", "subasta", "subasta_usuario", "reporte_producto", "historial_precio");

    @Autowired private EntityManager entityManager;
    @Autowired private ProductoRepo productoRepo;
    @Autowired private ComentarioRepo comentarioRepo;
    @Autowired private UsuarioRepo usuarioRepo;
    @Autowired private AdministradorRepo administradorRepo;
    @Autowired private SubastaRepo subastaRepo;
    @Autowired private SubastaUsuarioRepo subastaUsuarioRepo;
    @Autowired private CuponRepo cuponRepo;
    @Autowired private ReporteProductoRepo reporteProductoRepo;

    @Test
    public void todasLasTablasTienenAlMenosTresFilasTest() {
        Map<String, Long> menosDeTres = new TreeMap<>();
        for (String tabla : TABLAS) {
            long filas = ((Number) entityManager.createNativeQuery("select count(*) from " + tabla)
                    .getSingleResult()).longValue();
            if (filas < 3) {
                menosDeTres.put(tabla, filas);
            }
        }
        Assertions.assertTrue(menosDeTres.isEmpty(), "Tablas con menos de 3 filas: " + menosDeTres);
    }

    @Test
    public void noFaltanTablasEnLaListaTest() {
        @SuppressWarnings("unchecked")
        List<String> tablasBase = entityManager.createNativeQuery(
                "select table_name from information_schema.tables " +
                "where table_schema = database() and table_name <> 'hibernate_sequence'").getResultList();
        Assertions.assertEquals(new java.util.TreeSet<>(TABLAS), new java.util.TreeSet<>(tablasBase),
                "Si se agrega una entidad, hay que agregar su tabla a unishop.sql y a esta lista");
    }

    @Test
    public void datosCoherentesTest() {
        Date hoy = new Date();
        // 6 productos: el 6 (Lampara de Mesa) está vencido
        Assertions.assertEquals(6, productoRepo.count());
        Assertions.assertEquals(5, productoRepo.listarVigentes(hoy).size());
        // Cepillo Electrico: calificaciones 5 y 4
        Assertions.assertEquals(4.5, comentarioRepo.calificacionPromedio(1L));
        // Login con los datos del SQL
        Assertions.assertTrue(usuarioRepo.buscarPorEmailYPassword("lorena@email.com", "12345").isPresent());
        Assertions.assertTrue(administradorRepo.buscarPorEmailYPassword("profesor@uniquindio.com", "12345").isPresent());
        // 2 subastas activas; la puja más alta del Portatil es 2.100.000
        Assertions.assertEquals(2, subastaRepo.listarActivas(hoy).size());
        Assertions.assertEquals(2100000.0, subastaUsuarioRepo.pujaMasAlta(1L));
        // VERANO15 está vencido y agotado
        Assertions.assertTrue(cuponRepo.buscarDisponible("BIENVENIDA10", hoy).isPresent());
        Assertions.assertTrue(cuponRepo.buscarDisponible("VERANO15", hoy).isEmpty());
        Assertions.assertEquals(1, reporteProductoRepo.listarPorEstado(EstadoReporte.PENDIENTE).size());
    }
}
