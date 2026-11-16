package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepo usuarioRepo;
    private final AdministradorRepo administradorRepo;

    public UsuarioServicioImpl(UsuarioRepo usuarioRepo, AdministradorRepo administradorRepo) {
        this.usuarioRepo = usuarioRepo;
        this.administradorRepo = administradorRepo;
    }

    @Override
    public Usuario registrarUsuario(Usuario u) throws NegocioException {
        if (u.getId() != null && usuarioRepo.existsById(u.getId())) {
            throw new NegocioException("El usuario ya existe");
        }
        if (usuarioRepo.findByEmail(u.getEmail()).isPresent()
                || administradorRepo.findByEmail(u.getEmail()).isPresent()) {
            throw new NegocioException("El correo " + u.getEmail() + " ya se encuentra registrado");
        }
        return usuarioRepo.save(u);
    }

    @Override
    public Usuario actualizarUsuario(Usuario u) throws NegocioException {
        Usuario buscado = obtenerUsuario(u.getEmail());
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
