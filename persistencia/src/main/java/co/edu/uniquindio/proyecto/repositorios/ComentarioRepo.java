package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Comentario;
import co.edu.uniquindio.proyecto.entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepo extends JpaRepository<Comentario, Long> {

    @Query("select c from Comentario c inner join Producto p where p.id = :idProducto")
    public List<Comentario> FindAllWithDescriptionQueryProducto(Long idProducto);

    @Query("select c from Comentario c inner join Usuario u where u.id = :idUsuario")
    public List<Comentario> FindAllWithDescriptionQueryUsuario(Long idUsuario);

}
