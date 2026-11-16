package co.edu.uniquindio.proyecto.entidades;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Administrador de la tienda (hereda de {@link Persona}). Gestiona los usuarios, revisa los
 * reportes de productos ({@link ReporteProducto}) y consulta los reportes estadísticos.
 */
@Entity
@DiscriminatorValue("Administrador")
@NoArgsConstructor
public class Administrador extends Persona implements Serializable {


}
