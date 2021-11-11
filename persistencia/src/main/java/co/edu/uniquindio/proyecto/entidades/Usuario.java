package co.edu.uniquindio.proyecto.entidades;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity
@DiscriminatorValue("Usuario")
@NoArgsConstructor
@Getter
@Setter
public class Usuario extends Persona implements Serializable{

    @OneToMany(mappedBy="id_usuario")
    protected List<Chat> misChats;

    @ElementCollection
    @CollectionTable(name = "telefono")
    private List<String> numeroTelefono;

}
