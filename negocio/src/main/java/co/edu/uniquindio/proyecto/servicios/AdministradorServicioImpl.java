package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.seguridad.Contrasenas;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AdministradorServicioImpl implements AdministradorServicio {

    private final AdministradorRepo administradorRepo;
    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    public AdministradorServicioImpl(AdministradorRepo administradorRepo, UsuarioRepo usuarioRepo,
                                     PasswordEncoder passwordEncoder) {
        this.administradorRepo = administradorRepo;
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Administrador iniciarSesion(String email, String password) throws NegocioException {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new NegocioException("El correo y la contraseña son obligatorios");
        }
        return administradorRepo.findByEmail(normalizarEmail(email))
                .filter(a -> a.getPassword() != null && passwordEncoder.matches(password, a.getPassword()))
                .orElseThrow(() -> new NegocioException("Correo o contraseña incorrectos"));
    }

    /** Valida la contraseña (obligatoria y de máximo 72 bytes) y devuelve su hash BCrypt. */
    private String encriptar(String password) throws NegocioException {
        if (password == null || password.trim().isEmpty()) {
            throw new NegocioException("La contraseña es obligatoria");
        }
        Contrasenas.validarLongitud(password);
        return passwordEncoder.encode(password);
    }

    private static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public Administrador registrarAdministrador(Administrador a) throws NegocioException {
        a.setEmail(normalizarEmail(a.getEmail()));
        if (a.getId() != null && administradorRepo.existsById(a.getId())) {
            throw new NegocioException("El administrador ya existe");
        }
        if (administradorRepo.findByEmail(a.getEmail()).isPresent()
                || usuarioRepo.findByEmail(a.getEmail()).isPresent()) {
            throw new NegocioException("El correo " + a.getEmail() + " ya se encuentra registrado");
        }
        a.setPassword(encriptar(a.getPassword()));
        return administradorRepo.save(a);
    }

    @Override
    public Administrador actualizarAdministrador(Administrador a) throws NegocioException {
        a.setEmail(normalizarEmail(a.getEmail()));
        Administrador buscado = obtenerAdministrador(a.getEmail());
        buscado.setNombre(a.getNombre());
        // Si la contraseña viene vacía se conserva la actual; si viene, se encripta
        if (a.getPassword() != null && !a.getPassword().trim().isEmpty()) {
            buscado.setPassword(encriptar(a.getPassword()));
        }
        return administradorRepo.save(buscado);
    }

    @Override
    public Administrador obtenerAdministrador(String email) throws NegocioException {
        return administradorRepo.findByEmail(email)
                .orElseThrow(() -> new NegocioException("El administrador con correo " + email + " no existe"));
    }

    @Override
    public void eliminarAdministrador(String email) throws NegocioException {
        administradorRepo.delete(obtenerAdministrador(email));
    }

    @Override
    public List<Administrador> listarAdministrador() {
        return administradorRepo.findAll();
    }

    @Override
    public void cambiarPassword(String email, String nueva) throws NegocioException {
        if (nueva == null || nueva.trim().isEmpty()) {
            throw new NegocioException("La contraseña es obligatoria");
        }
        Administrador administrador = administradorRepo.findByEmail(normalizarEmail(email))
                .orElseThrow(() -> new NegocioException("El administrador con correo " + email + " no existe"));
        administrador.setPassword(encriptar(nueva));
        administradorRepo.save(administrador);
    }
}
