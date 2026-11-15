package co.edu.uniquindio.proyecto.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityManager;

/**
 * Base de las pruebas CRUD de cada entidad (Paso 1.6).
 * - Cada prueba corre en una transacción que se deshace al final (@DataJpaTest).
 * - Antes de cada prueba se cargan los datos de unishop.sql, así que se pueden usar sus ids conocidos.
 *   Los ids generados empiezan en 1000 (import.sql) y no chocan con los del SQL.
 * - Después de guardar se llama a {@link #sincronizar()} para que el INSERT/UPDATE/DELETE se ejecute de verdad
 *   en MySQL y la siguiente consulta lea de la base y no de la caché de Hibernate.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:unishop.sql")
public abstract class CrudTestBase {

    @Autowired
    protected EntityManager entityManager;

    /** Envía los cambios pendientes a la base y vacía la caché de Hibernate. */
    protected void sincronizar() {
        entityManager.flush();
        entityManager.clear();
    }
}
