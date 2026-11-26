package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/** Registra el filtro de seguridad para todas las páginas (las rutas privadas y la comprobación de cuenta activa). */
@Configuration
public class SeguridadConfig {

    @Bean
    public FilterRegistrationBean<SeguridadFiltro> seguridadFiltroRegistro(UsuarioServicio usuarioServicio) {
        FilterRegistrationBean<SeguridadFiltro> registro =
                new FilterRegistrationBean<>(new SeguridadFiltro(usuarioServicio));
        // Todas las páginas JSF; la raíz "/" reenvía (forward) a index.xhtml, por eso también se filtra el FORWARD
        registro.addUrlPatterns("*.xhtml");
        registro.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
        return registro;
    }
}
