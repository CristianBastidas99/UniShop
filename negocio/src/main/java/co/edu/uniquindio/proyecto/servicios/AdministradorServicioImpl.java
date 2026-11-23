package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AdministradorServicioImpl implements AdministradorServicio {

    private final AdministradorRepo administradorRepo;
    private final UsuarioRepo usuarioRepo;

    public AdministradorServicioImpl(AdministradorRepo administradorRepo, UsuarioRepo usuarioRepo) {
        this.administradorRepo = administradorRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public Administrador iniciarSesion(String email, String password) throws NegocioException {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new NegocioException("El correo y la contraseña son obligatorios");
        }
        return administradorRepo.buscarPorEmailYPassword(normalizarEmail(email), password)
                .orElseThrow(() -> new NegocioException("Correo o contraseña incorrectos"));
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
        return administradorRepo.save(a);
    }

    @Override
    public Administrador actualizarAdministrador(Administrador a) throws NegocioException {
        a.setEmail(normalizarEmail(a.getEmail()));
        Administrador buscado = obtenerAdministrador(a.getEmail());
        a.setId(buscado.getId());
        return administradorRepo.save(a);
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
        administrador.setPassword(nueva);
        administradorRepo.save(administrador);
    }
}
