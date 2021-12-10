package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Compra;
import co.edu.uniquindio.proyecto.entidades.DetalleCompra;

import java.util.List;

public interface DetalleCompraServicio {

    DetalleCompra registrarDetalleCompra(DetalleCompra dc) throws Exception;
    DetalleCompra actualizarDetalleCompra(DetalleCompra dc) throws Exception;
    DetalleCompra obtenerDetalleCompra(Long id) throws Exception;
    void eliminarDetalleCompra(Long id) throws Exception;
    List<DetalleCompra> listarDetalleCompra();

}
