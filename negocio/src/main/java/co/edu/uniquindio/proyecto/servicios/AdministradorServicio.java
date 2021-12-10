package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Producto;

import java.util.List;

public interface AdministradorServicio {

    Administrador registrarAdministrador(Administrador a) throws Exception;
    Administrador actualizarAdministrador(Administrador a) throws Exception;
    Administrador obtenerAdministrador(String email) throws Exception;
    void eliminarAdministrador(String email) throws Exception;
    List<Administrador> listarAdministrador();
}
