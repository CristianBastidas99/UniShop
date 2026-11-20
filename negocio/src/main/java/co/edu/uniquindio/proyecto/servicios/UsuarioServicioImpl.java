package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.repositorios.*;
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
    private final ProductoRepo productoRepo;
    private final CompraRepo compraRepo;
    private final ComentarioRepo comentarioRepo;
    private final ChatRepo chatRepo;
    private final MensajeRepo mensajeRepo;
    private final SubastaUsuarioRepo subastaUsuarioRepo;
    private final ReporteProductoRepo reporteProductoRepo;

    public UsuarioServicioImpl(UsuarioRepo usuarioRepo, AdministradorRepo administradorRepo, CiudadRepo ciudadRepo,
                               ProductoRepo productoRepo, CompraRepo compraRepo, ComentarioRepo comentarioRepo,
                               ChatRepo chatRepo, MensajeRepo mensajeRepo, SubastaUsuarioRepo subastaUsuarioRepo,
                               ReporteProductoRepo reporteProductoRepo) {
        this.usuarioRepo = usuarioRepo;
        this.administradorRepo = administradorRepo;
        this.ciudadRepo = ciudadRepo;
        this.productoRepo = productoRepo;
        this.compraRepo = compraRepo;
        this.comentarioRepo = comentarioRepo;
        this.chatRepo = chatRepo;
        this.mensajeRepo = mensajeRepo;
        this.subastaUsuarioRepo = subastaUsuarioRepo;
        this.reporteProductoRepo = reporteProductoRepo;
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
        Usuario usuario = usuarioRepo.buscarPorEmailYPassword(normalizarEmail(email), password)
                .orElseThrow(() -> new NegocioException("Correo o contraseña incorrectos"));
        if (!usuario.isActivo()) {
            throw new NegocioException("Tu cuenta está desactivada. Comunícate con el administrador");
        }
        return usuario;
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
        buscado.setNombre(u.getNombre());
        buscado.setCiudad(u.getCiudad());
        buscado.setNumeroTelefono(u.getNumeroTelefono());
        if (u.getPassword() != null && !u.getPassword().trim().isEmpty()) {
            buscado.setPassword(u.getPassword());
        }
        return usuarioRepo.save(buscado);
    }

    @Override
    public Usuario obtenerUsuario(String email) throws NegocioException {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new NegocioException("El usuario con correo " + email + " no existe"));
    }

    @Override
    public void eliminarUsuario(String email) throws NegocioException {
        Usuario usuario = obtenerUsuario(email);
        if (tieneActividad(usuario)) {
            throw new NegocioException("El usuario " + email + " tiene actividad (productos, compras, comentarios...) "
                    + "y no se puede eliminar; puedes desactivarlo");
        }
        usuarioRepo.delete(usuario);
    }

    @Override
    public void cambiarActivo(String email, boolean activo) throws NegocioException {
        Usuario usuario = obtenerUsuario(normalizarEmail(email));
        usuario.setActivo(activo);
        usuarioRepo.save(usuario);
    }

    @Override
    public boolean tieneActividad(String email) throws NegocioException {
        return tieneActividad(obtenerUsuario(normalizarEmail(email)));
    }

    private boolean tieneActividad(Usuario u) {
        Long id = u.getId();
        return productoRepo.existsByUsuarioId(id) || compraRepo.existsByUsuarioId(id)
                || comentarioRepo.existsByUsuarioId(id) || chatRepo.existsByUsuarioId(id)
                || mensajeRepo.existsByRemitenteId(id) || subastaUsuarioRepo.existsByUsuarioId(id)
                || reporteProductoRepo.existsByUsuarioId(id);
    }

    @Override
    public List<Usuario> buscarUsuarios(String texto) {
        String buscado = texto == null ? "" : texto.trim();
        if (buscado.isEmpty()) {
            return usuarioRepo.findAllByOrderByNombreAscIdAsc();
        }
        return usuarioRepo.buscarPorTexto(buscado);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepo.findAll();
    }
}
