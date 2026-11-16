package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.SubastaRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.SubastaUsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubastaUsuarioServicioTest extends BaseServicioTest {

    @Autowired
    private SubastaUsuarioServicio pujaServicio;

    @Autowired
    private SubastaRepo subastaRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    /** Puja del usuario 8 en la subasta indicada. */
    private SubastaUsuario nuevaPuja(Long idSubasta, double valor) {
        SubastaUsuario p = new SubastaUsuario();
        p.setValor(valor);
        p.setFechaSubasta(new Date());
        p.setSubasta(subastaRepo.findById(idSubasta).orElseThrow());
        p.setUsuario(usuarioRepo.findById(8L).orElseThrow());
        return p;
    }

    @Test
    public void registrarPujaTest() throws Exception {
        SubastaUsuario guardada = pujaServicio.registrarPuja(nuevaPuja(1L, 2200000));
        sincronizar();
        assertNotNull(guardada.getId());
        SubastaUsuario leida = pujaServicio.obtenerPuja(guardada.getId());
        assertEquals(2200000.0, leida.getValor());
        assertEquals(1L, leida.getSubasta().getId());
        assertEquals(8L, leida.getUsuario().getId());
    }

    @Test
    public void registrarConIdExistenteTest() {
        SubastaUsuario p = nuevaPuja(1L, 2200000);
        p.setId(1L);
        assertThrows(NegocioException.class, () -> pujaServicio.registrarPuja(p));
    }

    @Test
    public void actualizarPujaTest() throws Exception {
        SubastaUsuario p = pujaServicio.obtenerPuja(1L);
        p.setValor(2050000.0);
        pujaServicio.actualizarPuja(p);
        sincronizar();
        assertEquals(2050000.0, pujaServicio.obtenerPuja(1L).getValor());
    }

    @Test
    public void actualizarConIdNuloTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.actualizarPuja(nuevaPuja(1L, 2200000)));
    }

    @Test
    public void actualizarInexistenteTest() {
        SubastaUsuario p = nuevaPuja(1L, 2200000);
        p.setId(99999L);
        assertThrows(NegocioException.class, () -> pujaServicio.actualizarPuja(p));
    }

    @Test
    public void eliminarPujaTest() throws Exception {
        pujaServicio.eliminarPuja(4L);
        sincronizar();
        assertThrows(NegocioException.class, () -> pujaServicio.obtenerPuja(4L));
    }

    @Test
    public void eliminarInexistenteTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.eliminarPuja(99999L));
    }

    @Test
    public void listarPujasTest() {
        assertEquals(4, pujaServicio.listarPujas().size());
    }

    @Test
    public void listarPorSubastaTest() throws Exception {
        List<SubastaUsuario> pujas = pujaServicio.listarPorSubasta(1L);
        assertEquals(2, pujas.size());
        assertEquals(2100000.0, pujas.get(0).getValor());
        assertTrue(pujaServicio.listarPorSubasta(3L).isEmpty());
    }

    @Test
    public void listarPorSubastaInexistenteTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.listarPorSubasta(99999L));
    }

    @Test
    public void obtenerPujaMasAltaTest() throws Exception {
        assertEquals(2100000.0, pujaServicio.obtenerPujaMasAlta(1L));
        assertEquals(260000.0, pujaServicio.obtenerPujaMasAlta(2L));
    }

    @Test
    public void obtenerPujaMasAltaSinPujasTest() throws Exception {
        assertEquals(0.0, pujaServicio.obtenerPujaMasAlta(3L));
    }

    @Test
    public void obtenerPujaMasAltaSubastaInexistenteTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.obtenerPujaMasAlta(99999L));
        assertThrows(NegocioException.class, () -> pujaServicio.obtenerPujaMasAlta(null));
    }

    @Test
    public void obtenerInexistenteTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.obtenerPuja(99999L));
    }

    @Test
    public void obtenerNuloTest() {
        assertThrows(NegocioException.class, () -> pujaServicio.obtenerPuja(null));
    }
}
