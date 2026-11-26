package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Persona;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.seguridad.Contrasenas;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MigracionPasswordServicioImpl implements MigracionPasswordServicio {

    private final UsuarioRepo usuarioRepo;
    private final AdministradorRepo administradorRepo;
    private final PasswordEncoder passwordEncoder;

    public MigracionPasswordServicioImpl(UsuarioRepo usuarioRepo, AdministradorRepo administradorRepo,
                                         PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.administradorRepo = administradorRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public int encriptarPasswordsPendientes() {
        int cambiadas = 0;
        List<Usuario> usuarios = usuarioRepo.findAll();
        for (Usuario u : usuarios) {
            if (encriptarSiHaceFalta(u)) {
                usuarioRepo.save(u);
                cambiadas++;
            }
        }
        List<Administrador> administradores = administradorRepo.findAll();
        for (Administrador a : administradores) {
            if (encriptarSiHaceFalta(a)) {
                administradorRepo.save(a);
                cambiadas++;
            }
        }
        return cambiadas;
    }

    /** Encripta la contraseña de la persona si está en texto plano; devuelve si la cambió. */
    private boolean encriptarSiHaceFalta(Persona persona) {
        String actual = persona.getPassword();
        if (actual == null || actual.isEmpty() || Contrasenas.esBcrypt(actual)) {
            return false;
        }
        persona.setPassword(passwordEncoder.encode(actual));
        return true;
    }
}
