package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.CiudadRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepo usuarioRepo;
    private final AdministradorRepo administradorRepo;
    private final CiudadRepo ciudadRepo;

    public UsuarioServicioImpl(UsuarioRepo usuarioRepo, AdministradorRepo administradorRepo, CiudadRepo ciudadRepo) {
        this.usuarioRepo = usuarioRepo;
        this.administradorRepo = administradorRepo;
        this.ciudadRepo = ciudadRepo;
    }

    private static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Valida la ciudad (obligatoria y existente) y los teléfonos, y deja en el usuario los datos ya depurados. */
    private void validarCiudadYTelefonos(Usuario u) throws NegocioException {
        if (u.getCiudad() == null || u.getCiudad().getId() == null) {
            throw new NegocioException("La ciudad es obligatoria");
        }
        Ciudad ciudad = ciudadRepo.findById(u.getCiudad().getId())
                .orElseThrow(() -> new NegocioException("La ciudad no existe"));
        if (u.getNumeroTelefono() == null || u.getNumeroTelefono().isEmpty()) {
            throw new NegocioException("Debe registrar al menos un teléfono");
        }
        LinkedHashSet<String> telefonos = new LinkedHashSet<>();
        for (String telefono : u.getNumeroTelefono()) {
            String limpio = telefono == null ? "" : telefono.trim();
            if (!limpio.matches("\\+?\\d{7,15}")) {
                throw new NegocioException("El teléfono " + telefono + " no es válido");
            }
            telefonos.add(limpio);
        }
        u.setCiudad(ciudad);
        u.setNumeroTelefono(new ArrayList<>(telefonos));
    }

    @Override
    public Usuario iniciarSesion(String email, String password) throws NegocioException {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new NegocioException("El correo y la contraseña son obligatorios");
        }
        return usuarioRepo.buscarPorEmailYPassword(normalizarEmail(email), password)
                .orElseThrow(() -> new NegocioException("Correo o contraseña incorrectos"));
    }

    @Override
    public Usuario registrarUsuario(Usuario u) throws NegocioException {
        u.setEmail(normalizarEmail(u.getEmail()));
        if (u.getId() != null && usuarioRepo.existsById(u.getId())) {
            throw new NegocioException("El usuario ya existe");
        }
        if (usuarioRepo.findByEmail(u.getEmail()).isPresent()
                || administradorRepo.findByEmail(u.getEmail()).isPresent()) {
            throw new NegocioException("El correo " + u.getEmail() + " ya se encuentra registrado");
        }
        validarCiudadYTelefonos(u);
        return usuarioRepo.save(u);
    }

    @Override
    public Usuario actualizarUsuario(Usuario u) throws NegocioException {
        u.setEmail(normalizarEmail(u.getEmail()));
        Usuario buscado = obtenerUsuario(u.getEmail());
        validarCiudadYTelefonos(u);
        u.setId(buscado.getId());
        return usuarioRepo.save(u);
    }

    @Override
    public Usuario obtenerUsuario(String email) throws NegocioException {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new NegocioException("El usuario con correo " + email + " no existe"));
    }

    @Override
    public void eliminarUsuario(String email) throws NegocioException {
        usuarioRepo.delete(obtenerUsuario(email));
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepo.findAll();
    }
}
