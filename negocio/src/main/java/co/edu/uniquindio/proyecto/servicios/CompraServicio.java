package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.Usuario;

import java.util.List;

public interface CompraServicio {

    Compra registrarCompra(Compra c) throws Exception;
    Compra actualizarCompra(Compra c) throws Exception;
    Compra obtenerCompra(Long id) throws Exception;
    void eliminarCompra(Long id) throws Exception;
    List<Compra> listarCompra();


}



