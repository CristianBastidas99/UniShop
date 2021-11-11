package co.edu.uniquindio.proyecto.entidades;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue("Administrador")
@NoArgsConstructor
public class Administrador extends Persona implements Serializable {


}
