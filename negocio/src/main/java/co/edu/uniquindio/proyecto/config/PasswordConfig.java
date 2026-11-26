package co.edu.uniquindio.proyecto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Define el {@link PasswordEncoder} con el que los servicios encriptan y comparan las contraseñas.
 * Usa BCrypt (con sal aleatoria incluida en el hash): la contraseña nunca se guarda en texto plano.
 * Solo se usa el módulo spring-security-crypto, no Spring Security completo.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
