package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Producto;

import java.util.List;

public interface ComentarioServicio {

    Comentario registrarComentario(Comentario c) throws Exception;
    Comentario actualizarComentario(Comentario c) throws Exception;
    Comentario obtenerComentario(Long id) throws Exception;
    void eliminarComentario(Long id) throws Exception;
    List<Comentario> listarComentario();
    List<Comentario> listarComentarioProducto(Long idProducto);
    List<Comentario> listarComentarioUsuario(Long idUsuario);

}
