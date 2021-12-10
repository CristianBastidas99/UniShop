package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.entidades.Mensaje;
import co.edu.uniquindio.proyecto.entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepo extends JpaRepository<Mensaje, Long> {

    @Query("select m from Mensaje m inner join Chat c where c.id = :idChat")
    public List<Mensaje> FindAllWithDescriptionQueryChat(Long idChat);

}
