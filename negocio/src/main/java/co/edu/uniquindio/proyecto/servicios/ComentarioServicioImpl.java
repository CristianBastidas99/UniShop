package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Producto;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.ComentarioRepo;
import co.edu.uniquindio.proyecto.repositorios.ProductoRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ComentarioServicioImpl implements ComentarioServicio {

    private static final int MAX_MENSAJE = 1000;
    /** Largo de la columna {@code respuesta} (VARCHAR por defecto de JPA). */
    private static final int MAX_RESPUESTA = 255;

    private final ComentarioRepo comentarioRepo;
    private final UsuarioRepo usuarioRepo;
    private final ProductoRepo productoRepo;

    public ComentarioServicioImpl(ComentarioRepo comentarioRepo, UsuarioRepo usuarioRepo, ProductoRepo productoRepo) {
        this.comentarioRepo = comentarioRepo;
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
    }

    @Override
    public Comentario registrarComentario(Comentario comentario) throws NegocioException {
        if (comentario.getId() != null && comentarioRepo.existsById(comentario.getId())) {
            throw new NegocioException("El comentario ya existe");
        }
        return comentarioRepo.save(comentario);
    }

    @Override
    public Comentario actualizarComentario(Comentario comentario) throws NegocioException {
        if (comentario.getId() == null || !comentarioRepo.existsById(comentario.getId())) {
            throw new NegocioException("El comentario no existe");
        }
        return comentarioRepo.save(comentario);
    }

    @Override
    public Comentario obtenerComentario(Long id) throws NegocioException {
        if (id == null) {
            throw new NegocioException("El comentario no existe");
        }
        return comentarioRepo.findById(id)
                .orElseThrow(() -> new NegocioException("El comentario no existe"));
    }

    @Override
    public void eliminarComentario(Long id) throws NegocioException {
        Comentario buscado = obtenerComentario(id);
        comentarioRepo.delete(buscado);
    }

    @Override
    public List<Comentario> listarComentario() {
        return comentarioRepo.findAll();
    }

    @Override
    public List<Comentario> listarComentarioProducto(Long idProducto) {
        return comentarioRepo.listarPorProducto(idProducto);
    }

    @Override
    public List<Comentario> listarComentarioUsuario(Long idUsuario) {
        return comentarioRepo.listarPorUsuario(idUsuario);
    }

    @Override
    @Transactional
    public Comentario comentarProducto(Comentario comentario) throws NegocioException {
        if (comentario == null) {
            throw new NegocioException("El comentario es obligatorio");
        }
        if (comentario.getUsuario() == null || comentario.getUsuario().getId() == null) {
            throw new NegocioException("El usuario es obligatorio");
        }
        if (comentario.getProducto() == null || comentario.getProducto().getId() == null) {
            throw new NegocioException("El producto es obligatorio");
        }
        Usuario usuario = usuarioRepo.findById(comentario.getUsuario().getId())
                .orElseThrow(() -> new NegocioException("El usuario no existe"));
        Producto producto = productoRepo.findById(comentario.getProducto().getId())
                .orElseThrow(() -> new NegocioException("El producto no existe"));
        if (comentario.getMensaje() == null || comentario.getMensaje().trim().isEmpty()) {
            throw new NegocioException("El mensaje es obligatorio");
        }
        if (comentario.getMensaje().length() > MAX_MENSAJE) {
            throw new NegocioException("El mensaje no puede superar los 1000 caracteres");
        }
        if (comentario.getCalificacion() < 0 || comentario.getCalificacion() > 5) {
            throw new NegocioException("La calificación debe estar entre 0 y 5");
        }
        if (producto.getUsuario() != null && producto.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("No puede comentar su propio producto");
        }
        comentario.setId(null);
        comentario.setUsuario(usuario);
        comentario.setProducto(producto);
        comentario.setFecha(new Date());
        comentario.setRespuesta(null);
        return comentarioRepo.save(comentario);
    }

    @Override
    @Transactional
    public Comentario responderComentario(Long idComentario, String respuesta, Long idVendedor) throws NegocioException {
        Comentario comentario = obtenerComentario(idComentario);
        Usuario duenio = comentario.getProducto().getUsuario();
        if (idVendedor == null || duenio == null || !duenio.getId().equals(idVendedor)) {
            throw new NegocioException("Solo el vendedor del producto puede responder el comentario");
        }
        if (respuesta == null || respuesta.trim().isEmpty()) {
            throw new NegocioException("La respuesta es obligatoria");
        }
        if (respuesta.trim().length() > MAX_RESPUESTA) {
            throw new NegocioException("La respuesta no puede superar los 255 caracteres");
        }
        if (comentario.getRespuesta() != null && !comentario.getRespuesta().trim().isEmpty()) {
            throw new NegocioException("El comentario ya tiene respuesta");
        }
        comentario.setRespuesta(respuesta.trim());
        return comentarioRepo.save(comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public double calcularCalificacion(Long idProducto) throws NegocioException {
        if (idProducto == null || !productoRepo.existsById(idProducto)) {
            throw new NegocioException("El producto no existe");
        }
        Double promedio = comentarioRepo.calificacionPromedio(idProducto);
        if (promedio == null) {
            return 0;
        }
        return BigDecimal.valueOf(promedio).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Double> calcularCalificaciones(List<Long> idsProductos) {
        Map<Long, Double> resultado = new HashMap<>();
        if (idsProductos == null || idsProductos.isEmpty()) {
            return resultado;
        }
        List<Long> ids = idsProductos.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return resultado;
        }
        // Todos arrancan en 0.0; los que tienen comentarios se sobrescriben con su promedio
        ids.forEach(id -> resultado.put(id, 0.0));
        for (Object[] fila : comentarioRepo.calificacionPromedioPorProductos(ids)) {
            double promedio = ((Number) fila[1]).doubleValue();
            resultado.put((Long) fila[0], BigDecimal.valueOf(promedio).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }
        return resultado;
    }
}
