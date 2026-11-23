package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.demo.ResumenCargaDemo;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;

/**
 * Carga datos de demostración para poblar la aplicación: administradores, vendedores, compradores,
 * productos con imagen y actividad (compras, comentarios, favoritos, subastas con pujas, cupones y reportes).
 * Los datos salen de {@code demo/catalogo.json}.
 */
public interface CargaDemoServicio {

    /**
     * Crea los datos de demostración, todo o nada: si algo falla no queda nada guardado.
     * Es idempotente: si ya existe el correo del primer vendedor del catálogo, no hace nada y el resumen
     * lo indica con {@code yaCargado}.
     * <p>
     * Todo pasa por los servicios (validaciones, historial de precios...). Las compras se hacen con
     * {@code CompraServicio.comprar}, que publica el correo de la compra: al cargar en la aplicación saldrán
     * unas 24 notificaciones al servidor de correo (en desarrollo, Mailpit).
     *
     * @return conteos de lo creado
     * @throws NegocioException si el catálogo es inválido o alguna regla de negocio se incumple
     */
    ResumenCargaDemo cargar() throws NegocioException;
}
