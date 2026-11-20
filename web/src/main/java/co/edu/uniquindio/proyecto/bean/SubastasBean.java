package co.edu.uniquindio.proyecto.bean;

import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Subasta;
import co.edu.uniquindio.proyecto.entidades.SubastaUsuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.servicios.SubastaServicio;
import co.edu.uniquindio.proyecto.servicios.SubastaUsuarioServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean de {@code subastas.xhtml}: lista pública de subastas activas, ya copiadas a datos simples
 * (la que termina primero va primero).
 */
@Component
@ViewScoped
public class SubastasBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SubastasBean.class);
    private static final String IMAGEN_POR_DEFECTO = "resources/img/sin-imagen.svg";

    /** Subasta activa copiada a datos simples. */
    public static class Tarjeta implements Serializable {
        private final Long id;
        private final String nombre;
        private final String imagen;
        private final Double pujaMasAlta;
        private final int cantidadPujas;
        private final long restanteMs;

        Tarjeta(Long id, String nombre, String imagen, Double pujaMasAlta, int cantidadPujas, long restanteMs) {
            this.id = id;
            this.nombre = nombre;
            this.imagen = imagen;
            this.pujaMasAlta = pujaMasAlta;
            this.cantidadPujas = cantidadPujas;
            this.restanteMs = restanteMs;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getImagen() { return imagen; }
        public Double getPujaMasAlta() { return pujaMasAlta; }
        public boolean isConPujas() { return cantidadPujas > 0; }
        public int getCantidadPujas() { return cantidadPujas; }
        public long getRestanteMs() { return restanteMs; }
    }

    private final SubastaServicio subastaServicio;
    private final SubastaUsuarioServicio pujaServicio;

    private List<Tarjeta> subastas = new ArrayList<>();

    public SubastasBean(SubastaServicio subastaServicio, SubastaUsuarioServicio pujaServicio) {
        this.subastaServicio = subastaServicio;
        this.pujaServicio = pujaServicio;
    }

    /** Se ejecuta con f:viewAction al abrir la página. */
    public void cargar() {
        subastas = new ArrayList<>();
        String contexto = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        long ahora = System.currentTimeMillis();
        for (Subasta s : subastaServicio.listarActivas()) {
            try {
                Producto p = s.getProducto();
                String imagen = p.getMisImagenes() == null || p.getMisImagenes().isEmpty()
                        ? IMAGEN_POR_DEFECTO : p.getMisImagenes().get(0);
                List<SubastaUsuario> pujas = pujaServicio.listarPorSubasta(s.getId());
                Double maxima = pujas.isEmpty() ? null : pujas.get(0).getValor();
                subastas.add(new Tarjeta(s.getId(), p.getNombre(), contexto + "/" + imagen, maxima,
                        pujas.size(), Math.max(0, s.getFechaLimite().getTime() - ahora)));
            } catch (NegocioException e) {
                LOG.warn("Subasta {} omitida: {}", s.getId(), e.getMessage());
            }
        }
    }

    public List<Tarjeta> getSubastas() { return subastas; }
}
