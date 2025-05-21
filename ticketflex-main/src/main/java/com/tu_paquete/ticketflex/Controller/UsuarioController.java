package com.tu_paquete.ticketflex.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Repository.Mongo.UsuarioRepository;
import com.tu_paquete.ticketflex.Service.TransaccionService;
import com.tu_paquete.ticketflex.Service.UsuarioService;
import com.tu_paquete.ticketflex.dto.PurchaseDTO;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private TransaccionService transaccionService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        Map<String, String> response = new HashMap<>();
        try {
            // Validación básica
            if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty() ||
                    usuario.getEmail() == null || usuario.getEmail().trim().isEmpty() ||
                    usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {

                response.put("error", "Nombre, email y contraseña son obligatorios");
                return ResponseEntity.badRequest().body(response);
            }

            Usuario usuarioRegistrado = usuarioService.registrarUsuario(usuario);
            response.put("mensaje", "Usuario registrado exitosamente");
            response.put("idUsuario", usuarioRegistrado.getId().toString());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> iniciarSesion(@RequestParam String email,
            @RequestParam String password,
            HttpSession session) {
        Usuario usuario = usuarioService.iniciarSesion(email, password);
        if (usuario != null) {
            // Guardamos usuario en sesión
            session.setAttribute("usuario", usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("nombre", usuario.getNombre());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(null);
    }

    // Manejar solicitudes GET con un mensaje adecuado
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<String> loginGet() {
        // Puedes devolver un mensaje de error o una página de error
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Método GET no permitido.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> cerrarSesion(HttpSession session) {
        session.invalidate(); // Invalida toda la sesión
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }

    @GetMapping("/{id}/historial")
public ResponseEntity<List<PurchaseDTO>> obtenerHistorialDeCompras(@PathVariable String id) {
    try {
        List<PurchaseDTO> historial = transaccionService.obtenerHistorialDeComprasDTO(id);
        if (historial.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        return ResponseEntity.ok(historial);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

    @GetMapping("/verificarSesion")
    public ResponseEntity<?> verificarSesion(@SessionAttribute(name = "usuario", required = false) Usuario usuario) {
        if (usuario != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("nombre", usuario.getNombre());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No has iniciado sesión");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable String id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPerfil(@PathVariable String id, @RequestBody Usuario nuevosDatos) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);

        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();

            // Actualizar solo los campos que quieres permitir
            usuario.setNombre(nuevosDatos.getNombre());
            usuario.setApellido(nuevosDatos.getApellido());
            usuario.setTelefono(nuevosDatos.getTelefono());
            usuario.setDireccion(nuevosDatos.getDireccion());

            usuarioRepository.save(usuario);

            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }

    @PutMapping("/{id}/cambiar-contrasena")
    public ResponseEntity<?> cambiarContrasena(@PathVariable String id,
            @RequestBody Map<String, String> contrasenas) {
        String actual = contrasenas.get("currentPassword");
        String nueva = contrasenas.get("newPassword");

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(actual, usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("La contraseña actual es incorrecta.");
        }

        if (nueva.length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        usuario.setPassword(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario); // MongoRepository guarda el cambio

        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }

    

}
