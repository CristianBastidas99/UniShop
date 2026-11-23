package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.CantidadDTO;
import co.edu.uniquindio.proyecto.dto.MedioPagoDTO;
import co.edu.uniquindio.proyecto.dto.PromedioDTO;
import co.edu.uniquindio.proyecto.dto.VentaMesDTO;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

import java.util.List;

/**
 * Reportes estadísticos de UniShop para los gráficos del panel de administración.
 */
public interface ReporteServicio {

    /** Cantidad de elementos que devuelven por defecto los reportes con límite. */
    int TOP_POR_DEFECTO = 5;

    /**
     * Reporte 1 (gráfico de pastel): cantidad de productos por categoría, incluidas las categorías sin
     * productos, ordenadas por nombre.
     */
    List<CantidadDTO> productosPorCategoria();

    /**
     * Reporte 2 (gráfico de líneas): dinero vendido por año y mes. El precio de cada detalle ya incluye el
     * descuento del producto y el del cupón.
     */
    List<VentaMesDTO> ventasPorMes();

    /**
     * Reporte 3 (gráfico de barras): vendedores con más unidades vendidas.
     *
     * @param limite cantidad de vendedores a devolver, entre 1 y 50
     * @throws NegocioException si el límite está fuera del rango
     */
    List<CantidadDTO> topVendedores(int limite) throws NegocioException;

    /**
     * Igual que {@link #topVendedores(int)} usando {@link #TOP_POR_DEFECTO}.
     */
    default List<CantidadDTO> topVendedores() throws NegocioException {
        return topVendedores(TOP_POR_DEFECTO);
    }

    /**
     * Reporte 4 (barras horizontales): productos con mejor calificación promedio, redondeada a 1 decimal.
     * Los productos sin comentarios no aparecen.
     *
     * @param limite cantidad de productos a devolver, entre 1 y 50
     * @throws NegocioException si el límite está fuera del rango
     */
    List<PromedioDTO> mejorCalificados(int limite) throws NegocioException;

    /**
     * Igual que {@link #mejorCalificados(int)} usando {@link #TOP_POR_DEFECTO}.
     */
    default List<PromedioDTO> mejorCalificados() throws NegocioException {
        return mejorCalificados(TOP_POR_DEFECTO);
    }

    /**
     * Reporte 5 (gráfico de dona): cantidad de compras por medio de pago.
     */
    List<MedioPagoDTO> comprasPorMedioPago();
}
