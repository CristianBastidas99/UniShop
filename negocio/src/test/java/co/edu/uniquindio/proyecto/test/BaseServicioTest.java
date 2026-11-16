package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.NegocioApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Base común de las pruebas de los servicios: levanta Spring, carga los datos de prueba (unishop.sql)
 * y deshace los cambios al terminar cada prueba.
 */
@SpringBootTest(classes = NegocioApplication.class, properties = "unishop.tareas.habilitadas=false")
@Transactional
@Sql("classpath:unishop.sql")
public abstract class BaseServicioTest {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Envía a MySQL los cambios pendientes ({@code flush}) y vacía la caché de Hibernate ({@code clear}).
     * Sin esto, por el rollback de {@code @Transactional} el INSERT/UPDATE nunca se ejecuta y la
     * verificación leería el objeto de la caché en lugar de la base.
     */
    protected void sincronizar() {
        entityManager.flush();
        entityManager.clear();
    }
}
