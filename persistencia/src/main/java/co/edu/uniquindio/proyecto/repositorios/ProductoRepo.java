package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.dto.CantidadDTO;
import co.edu.uniquindio.proyecto.dto.PromedioDTO;
import co.edu.uniquindio.proyecto.entidades.Producto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProductoRepo extends JpaRepository<Producto, Long> {

    /** Productos vigentes: fecha límite posterior a {@code hoy} y con unidades disponibles. */
    @Query("select p from Producto p where p.fechaLimite > :hoy and p.unidades > 0")
    List<Producto> listarVigentes(@Param("hoy") Date hoy);

    /** Productos vigentes de una categoría. */
    @Query("select p from Categoria c join c.misProductos p " +
            "where c.id = :idCategoria and p.fechaLimite > :hoy and p.unidades > 0")
    List<Producto> listarVigentesPorCategoria(@Param("idCategoria") Long idCategoria, @Param("hoy") Date hoy);

    /**
     * Búsqueda de productos vigentes. Todos los filtros son opcionales (si llegan en {@code null} no se aplican):
     * texto contenido en el nombre (sin distinguir mayúsculas), categoría, rango de precio, ciudad y
     * calificación promedio mínima (un producto sin comentarios tiene promedio 0).
     */
    @Query("select p from Producto p where p.fechaLimite > :hoy and p.unidades > 0 " +
            "and (:texto is null or lower(p.nombre) like lower(concat('%', :texto, '%'))) " +
            "and (:idCategoria is null or p in (select cp from Categoria cat join cat.misProductos cp where cat.id = :idCategoria)) " +
            "and (:precioMin is null or p.precio >= :precioMin) " +
            "and (:precioMax is null or p.precio <= :precioMax) " +
            "and (:idCiudad is null or p.ciudad.id = :idCiudad) " +
            "and (:calificacionMin is null or " +
            "     (select coalesce(avg(co.calificacion), 0) from Comentario co where co.producto = p) >= :calificacionMin)")
    List<Producto> buscar(@Param("texto") String texto,
                          @Param("idCategoria") Long idCategoria,
                          @Param("precioMin") Double precioMin,
                          @Param("precioMax") Double precioMax,
                          @Param("idCiudad") Long idCiudad,
                          @Param("calificacionMin") Double calificacionMin,
                          @Param("hoy") Date hoy);

    /** Productos favoritos del usuario con ese email. */
    @Query("select f from Usuario u join u.misProductosFavoritos f where u.email = :email")
    List<Producto> listarFavoritos(@Param("email") String email);

    /** Productos publicados por un vendedor (vigentes o no). */
    @Query("select p from Producto p where p.usuario.id = :idUsuario")
    List<Producto> listarPorVendedor(@Param("idUsuario") Long idUsuario);

    // ===== Reportes del administrador (Fase 9) =====

    /** Reporte 1: cantidad de productos publicados por categoría (incluye categorías sin productos). */
    @Query("select new co.edu.uniquindio.proyecto.dto.CantidadDTO(c.nombre, count(p)) " +
            "from Categoria c left join c.misProductos p group by c.id, c.nombre order by c.nombre")
    List<CantidadDTO> contarPorCategoria();

    /** Reporte 4: productos con mejor calificación promedio (usar {@code PageRequest.of(0, 5)} para el top 5). */
    @Query("select new co.edu.uniquindio.proyecto.dto.PromedioDTO(p.nombre, avg(c.calificacion)) " +
            "from Comentario c join c.producto p group by p.id, p.nombre order by avg(c.calificacion) desc, p.nombre")
    List<PromedioDTO> mejorCalificados(Pageable pageable);
}
