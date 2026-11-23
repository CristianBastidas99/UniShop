package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.TokenRecuperacion;
import co.edu.uniquindio.proyecto.repositorios.TokenRecuperacionRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TokenRecuperacionTest extends CrudTestBase {

    private static final String HASH_NUEVO = "d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3";

    @Autowired
    private TokenRecuperacionRepo tokenRepo;

    @Test
    public void registrarTokenTest() {
        TokenRecuperacion t = new TokenRecuperacion();
        t.setTokenHash(HASH_NUEVO);
        t.setEmail("lorena@email.com");
        t.setFechaCreacion(new Date());
        t.setFechaExpiracion(new Date(System.currentTimeMillis() + 30 * 60 * 1000));

        TokenRecuperacion guardado = tokenRepo.saveAndFlush(t);
        sincronizar();

        TokenRecuperacion buscado = tokenRepo.findById(guardado.getId()).orElseThrow();
        Assertions.assertEquals(HASH_NUEVO, buscado.getTokenHash());
        Assertions.assertEquals("lorena@email.com", buscado.getEmail());
        Assertions.assertFalse(buscado.isUsado());
        Assertions.assertNull(buscado.getFechaUso());
        Assertions.assertTrue(tokenRepo.findByTokenHash(HASH_NUEVO).isPresent());
    }

    @Test
    public void eliminarTokenTest() {
        tokenRepo.deleteById(3L);
        sincronizar();

        Assertions.assertTrue(tokenRepo.findById(3L).isEmpty());
        Assertions.assertEquals(2, tokenRepo.count());
    }

    @Test
    public void actualizarTokenTest() {
        // Se marca como usado el token 1
        TokenRecuperacion t = tokenRepo.findById(1L).orElseThrow();
        Date ahora = new Date();
        t.setUsado(true);
        t.setFechaUso(ahora);
        tokenRepo.saveAndFlush(t);
        sincronizar();

        TokenRecuperacion buscado = tokenRepo.findById(1L).orElseThrow();
        Assertions.assertTrue(buscado.isUsado());
        Assertions.assertNotNull(buscado.getFechaUso());
        // Ya no quedan tokens vigentes de ese correo (el 3 sigue sin usar, aunque vencido)
        List<TokenRecuperacion> vigentes = tokenRepo.findByEmailAndUsadoFalse("lorena@email.com");
        Assertions.assertEquals(1, vigentes.size());
        Assertions.assertEquals(3L, vigentes.get(0).getId());
    }

    @Test
    public void listarTokenTest() {
        Set<Long> ids = tokenRepo.findAll().stream().map(TokenRecuperacion::getId).collect(Collectors.toSet());
        Assertions.assertEquals(Set.of(1L, 2L, 3L), ids);
    }
}
