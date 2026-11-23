package co.edu.uniquindio.proyecto.bean;

import org.springframework.stereotype.Component;

/**
 * Versión de los recursos estáticos propios (como {@code estilos.css}) para las URL de las páginas.
 *
 * <p>JSF sirve sus recursos con {@code Cache-Control: max-age=604800} (una semana), así que tras un cambio en el CSS
 * el navegador seguía usando la copia vieja. El template pide el CSS con {@code ?v=#{recursosBean.version}}: la
 * versión cambia en cada arranque de la aplicación, y con ella la URL, así que el navegador descarga la copia nueva.
 */
@Component
public class RecursosBean {

    private final String version = Long.toString(System.currentTimeMillis(), 36);

    /** Versión de los recursos: distinta en cada arranque de la aplicación. */
    public String getVersion() {
        return version;
    }
}
