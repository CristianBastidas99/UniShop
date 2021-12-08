package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Usuario;

import java.util.List;

public interface UsuarioServicio {

    Usuario registrarUsuario(Usuario u) throws Exception;
    Usuario actualizarUsuario(Usuario u) throws Exception;
    Usuario obtenerUsuario(String email) throws Exception;
    void eliminarUsuario(String email) throws Exception;
    List<Usuario> listarUsuarios();

}
