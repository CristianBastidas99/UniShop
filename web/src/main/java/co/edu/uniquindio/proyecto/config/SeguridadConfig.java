package co.edu.uniquindio.proyecto.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra el filtro de seguridad para las rutas privadas. */
@Configuration
public class SeguridadConfig {

    @Bean
    public FilterRegistrationBean<SeguridadFiltro> seguridadFiltroRegistro() {
        FilterRegistrationBean<SeguridadFiltro> registro = new FilterRegistrationBean<>(new SeguridadFiltro());
        registro.addUrlPatterns("/usuario/*", "/admin/*");
        return registro;
    }
}
