package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.entidades.Producto;

import java.util.List;

public interface MensajeServicio {

    Mensaje registrarMensaje(Mensaje m) throws Exception;
    Mensaje actualizarMensaje(Mensaje m) throws Exception;
    Mensaje obtenerMensaje(Long id) throws Exception;
    void eliminarMensaje(Long id) throws Exception;
    List<Mensaje> listarMensaje();
    List<Mensaje> listarMensajeChat(Long idChat);

}
