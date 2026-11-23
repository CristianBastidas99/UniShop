package co.edu.uniquindio.proyecto.dto.demo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Conteos de lo que creó la carga de datos de demostración. */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResumenCargaDemo {

    /** {@code true} si los datos ya estaban cargados y no se creó nada. */
    private boolean yaCargado;
    private int administradores;
    private int vendedores;
    private int compradores;
    private int productos;
    private int cupones;
    private int compras;
    private int comprasConCupon;
    private int comentarios;
    private int respuestas;
    private int favoritos;
    private int subastas;
    private int pujas;
    private int reportes;

    /** Texto breve para el registro de la aplicación. */
    public String describir() {
        if (yaCargado) {
            return "Los datos de demostración ya estaban cargados";
        }
        return String.format("%d administradores, %d vendedores, %d compradores, %d productos, %d cupones, "
                        + "%d compras (%d con cupón), %d comentarios (%d con respuesta), %d favoritos, "
                        + "%d subastas con %d pujas y %d reportes",
                administradores, vendedores, compradores, productos, cupones, compras, comprasConCupon,
                comentarios, respuestas, favoritos, subastas, pujas, reportes);
    }
}
