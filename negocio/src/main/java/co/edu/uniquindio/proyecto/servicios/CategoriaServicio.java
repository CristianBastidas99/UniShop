package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Categoria;

import java.util.List;

public interface CategoriaServicio {

    Categoria registrarCategoria(Categoria c) throws Exception;
    Categoria actualizarCategoria(Categoria c) throws Exception;
    Categoria obtenerCategoria(Long id) throws Exception;
    void eliminarCategoria(Long id) throws Exception;
    List<Categoria> listarCategoria();

}
