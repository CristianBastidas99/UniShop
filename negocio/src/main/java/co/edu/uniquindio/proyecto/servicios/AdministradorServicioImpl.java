package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Administrador;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.repositorios.AdministradorRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdministradorServicioImpl implements  AdministradorServicio{

    private final AdministradorRepo administradorRepo;

    public AdministradorServicioImpl(AdministradorRepo administradorRepo) {
        this.administradorRepo = administradorRepo;
    }


    @Override
    public Administrador registrarAdministrador(Administrador a) throws Exception {
        Optional<Administrador> buscado = administradorRepo.findByEmail(a.getEmail());
        if (buscado.isPresent()) {
            throw new Exception("El correo ya se encuentra registrado");
        }
        return administradorRepo.save(a);
    }

    @Override
    public Administrador actualizarAdministrador(Administrador a) throws Exception {
        Optional<Administrador> buscado = administradorRepo.findByEmail(a.getEmail());
        if (!buscado.isPresent()) {
            throw new Exception("El administrador no existe");
        }
        a.setId(buscado.get().getId());
        return administradorRepo.save(a);
    }

    @Override
    public Administrador obtenerAdministrador(String email) throws Exception {
        Optional<Administrador> buscado = administradorRepo.findByEmail(email);
        if (!buscado.isPresent()) {
            throw new Exception("El administrador que busca no existe");
        }
        return buscado.get();
    }

    @Override
    public void eliminarAdministrador(String email) throws Exception {
        Optional<Administrador> buscado = administradorRepo.findByEmail(email);
        if (!buscado.isPresent()) {
            throw new Exception("El administrador no existe");
        }
        administradorRepo.delete(buscado.get());
    }

    @Override
    public List<Administrador> listarAdministrador() {
        return administradorRepo.findAll();
    }
}

