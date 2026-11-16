package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministradorServicioImpl implements AdministradorServicio {

    private final AdministradorRepo administradorRepo;
    private final UsuarioRepo usuarioRepo;

    public AdministradorServicioImpl(AdministradorRepo administradorRepo, UsuarioRepo usuarioRepo) {
        this.administradorRepo = administradorRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public Administrador registrarAdministrador(Administrador a) throws NegocioException {
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
}
