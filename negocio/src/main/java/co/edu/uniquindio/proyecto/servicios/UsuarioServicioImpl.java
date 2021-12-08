package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServicioImpl implements  UsuarioServicio{

    private final UsuarioRepo usuarioRepo;
    public UsuarioServicioImpl(UsuarioRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }


    @Override
    public Usuario registrarUsuario(Usuario u) throws Exception {
        Optional<Usuario> buscado = usuarioRepo.findByEmail(u.getEmail());
        if (buscado.isPresent()) {
            throw new Exception("El correo ya se encuentra registrado");
        }
        return usuarioRepo.save(u);
    }

    @Override
    public Usuario actualizarUsuario(Usuario u) throws Exception {
        Optional<Usuario> buscado = usuarioRepo.findByEmail(u.getEmail());
        if (!buscado.isPresent()) {
            throw new Exception("El usuario no existe");
        }
        u.setId(buscado.get().getId());
        return usuarioRepo.save(u);
    }

    @Override
    public Usuario obtenerUsuario(String email) throws Exception {
        Optional<Usuario> buscado = usuarioRepo.findByEmail(email);
        if (!buscado.isPresent()) {
            return null;
        }
        return buscado.get();
    }

    @Override
    public void eliminarUsuario(String email) throws Exception {
        Optional<Usuario> buscado = usuarioRepo.findByEmail(email);
        if (!buscado.isPresent()) {
            throw new Exception("El usuario no existe");
        }
        usuarioRepo.delete(buscado.get());
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepo.findAll();
    }
}
