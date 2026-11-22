package co.edu.uniquindio.proyecto.rest;

import co.edu.uniquindio.proyecto.entidades.Ciudad;
import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.excepciones.NegocioException;
import co.edu.uniquindio.proyecto.rest.dto.usuario.UsuarioDTO;
import co.edu.uniquindio.proyecto.rest.dto.usuario.UsuarioEntradaDTO;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * API REST de usuarios ({@code /api/usuarios}). Recibe y devuelve DTOs (nunca la contraseña).
 * Los errores los convierte {@link ManejadorErroresRest} en {@code { "mensaje": "..." }}.
 * <p>
 * Importante: la API no tiene autenticación; cualquiera que llegue al servidor puede usarla.
 * El correo va en la ruta (puede llevar puntos) y se normaliza a minúsculas.
 */
@RestController
@RequestMapping("api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    private static String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Convierte el DTO de entrada en entidad (la ciudad solo lleva el id: el servicio la busca). */
    private static Usuario aEntidad(UsuarioEntradaDTO dto, String email) {
        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setEmail(email);
        u.setPassword(dto.getPassword());
        Ciudad ciudad = new Ciudad();
        ciudad.setId(dto.getIdCiudad());
        u.setCiudad(ciudad);
        u.setNumeroTelefono(new ArrayList<>(dto.getTelefonos()));
        return u;
    }

    /** {@code GET /api/usuarios}: lista todos los usuarios (200). */
    @GetMapping
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioServicio.listarUsuarios().stream().map(UsuarioDTO::desde).collect(Collectors.toList());
    }

    /** {@code GET /api/usuarios/{email}}: 200 con el usuario, o 404 si no existe. */
    @GetMapping("/{email:.+}")
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuario(@PathVariable("email") String email) throws NegocioException {
        return UsuarioDTO.desde(usuarioServicio.obtenerUsuario(normalizar(email)));
    }

    /**
     * {@code POST /api/usuarios}: crea un usuario (201 con el usuario creado). Correo y contraseña son
     * obligatorios; 400 si hay datos inválidos, el correo ya existe o la ciudad no existe.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<UsuarioDTO> crearUsuario(@Valid @RequestBody UsuarioEntradaDTO dto) throws NegocioException {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new NegocioException("email: no debe estar vacío");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new NegocioException("password: no debe estar vacío");
        }
        Usuario creado = usuarioServicio.registrarUsuario(aEntidad(dto, normalizar(dto.getEmail())));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO.desde(creado));
    }

    /**
     * {@code PUT /api/usuarios/{email}}: actualiza nombre, ciudad y teléfonos (y la contraseña solo si viene).
     * 200 con el usuario actualizado, 404 si no existe, 400 si los datos no son válidos.
     */
    @PutMapping("/{email:.+}")
    @Transactional
    public UsuarioDTO actualizarUsuario(@PathVariable("email") String email,
                                        @Valid @RequestBody UsuarioEntradaDTO dto) throws NegocioException {
        return UsuarioDTO.desde(usuarioServicio.actualizarUsuario(aEntidad(dto, normalizar(email))));
    }

    /**
     * {@code DELETE /api/usuarios/{email}}: elimina el usuario (204). 404 si no existe; 400 si tiene
     * actividad y no se puede eliminar.
     */
    @DeleteMapping("/{email:.+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarUsuario(@PathVariable("email") String email) throws NegocioException {
        usuarioServicio.eliminarUsuario(normalizar(email));
    }
}
