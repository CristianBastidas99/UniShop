package co.edu.uniquindio.proyecto.rest;

import co.edu.uniquindio.proyecto.entidades.Usuario;
import co.edu.uniquindio.proyecto.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @GetMapping
    public List<Usuario> listarUsuarios(){
        return usuarioServicio.listarUsuarios();
    }

    @GetMapping(value ="/{email}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable(name = "email") String email){
        try {
            return ResponseEntity.status(200).body(usuarioServicio.obtenerUsuario(email));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new Mensaje(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario){
        try {
            usuarioServicio.registrarUsuario(usuario);
            return ResponseEntity.status(201).body(new Mensaje("Usuario creado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new Mensaje(e.getMessage()));
        }
    }


    public void borrarUsuario(Integer id){
    }
    public void actualizarUsuario(Usuario usuario){
    }

}
