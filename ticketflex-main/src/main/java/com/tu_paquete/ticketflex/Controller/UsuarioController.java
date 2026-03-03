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

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.BoletoService;
import com.tu_paquete.ticketflex.Service.UsuarioService;
import com.tu_paquete.ticketflex.repository.mongo.UsuarioRepository;
import com.tu_paquete.ticketflex.segurity.JwtUtil;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BoletoService boletoService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        Map<String, String> response = new HashMap<>();
        try {
            Usuario usuarioRegistrado = usuarioService.registrarUsuario(usuario);
            response.put("mensaje", "Usuario registrado exitosamente");
            response.put("idUsuario", usuarioRegistrado.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> iniciarSesion(
            @RequestBody Map<String, String> loginRequest,
            jakarta.servlet.http.HttpServletResponse response) { // <--- Añadimos el response

        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Optional<Usuario> usuarioOpt = usuarioService.iniciarSesion(email, password);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String rol = usuario.getRol().getNombreRol();
            String token = jwtUtil.generarToken(usuario.getId(), rol);

            // --- CONFIGURACIÓN DE LA COOKIE SEGURA ---
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("token", token);
            cookie.setHttpOnly(true); // Bloquea que JavaScript robe el token (Anti-XSS)
            cookie.setSecure(false); // Ponlo en 'true' cuando subas a Render (HTTPS)
            cookie.setPath("/"); // Disponible en toda la web
            cookie.setMaxAge(86400); // Dura 24 horas

            response.addCookie(cookie); // Se envía al navegador automáticamente

            // Respuesta JSON normal para tu lógica de Frontend
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", token);
            responseBody.put("id", usuario.getId());
            responseBody.put("nombre", usuario.getNombre());
            responseBody.put("rol", rol);

            return ResponseEntity.ok(responseBody);
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Usuario o contraseña incorrecta");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> cerrarSesion() {
        // En JWT el logout se maneja en el frontend borrando el token
        return ResponseEntity.ok("Sesión cerrada correctamente (borre el token en el cliente)");
    }

    @GetMapping("/verificarSesion")
    public ResponseEntity<?> verificarSesion(@RequestAttribute(name = "userId", required = false) String userId,
            @RequestAttribute(name = "rol", required = false) String rol) {
        if (userId != null) {
            return usuarioRepository.findById(userId)
                    .map(usuario -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("id", usuario.getId());
                        response.put("nombre", usuario.getNombre());
                        response.put("rol", rol);
                        // Aquí Java entiende que devuelve ResponseEntity<Map>
                        return ResponseEntity.ok((Object) response);
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body((Object) "Usuario no encontrado"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<Boleto>> obtenerHistorialDeCompras(@PathVariable String id) {
        List<Boleto> historial = boletoService.findByUsuarioId(id);
        return historial.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(historial);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable String id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPerfil(@PathVariable String id, @RequestBody Usuario nuevosDatos) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombre(nuevosDatos.getNombre());
                    usuario.setApellido(nuevosDatos.getApellido());
                    usuario.setTelefono(nuevosDatos.getTelefono());
                    usuario.setDireccion(nuevosDatos.getDireccion());

                    usuarioRepository.save(usuario);
                    // Retornamos el objeto usuario, pero como parte de ResponseEntity<?>
                    return ResponseEntity.ok((Object) usuario);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body((Object) "Usuario no encontrado"));
    }

    @PutMapping("/{id}/cambiar-contrasena")
    public ResponseEntity<?> cambiarContrasena(@PathVariable String id, @RequestBody Map<String, String> contrasenas) {
        String actual = contrasenas.get("currentPassword");
        String nueva = contrasenas.get("newPassword");

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(actual, usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("La contraseña actual es incorrecta.");
        }

        if (nueva.length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La contraseña debe tener al menos 6 caracteres.");
        }

        usuario.setPassword(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();
        try {
            usuarioService.generarTokenDeReseteo(email, false);
            response.put("message", "Si el correo está registrado, recibirás un enlace de reseteo.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error al procesar la solicitud.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String nuevaPassword = request.get("newPassword");
        Map<String, String> response = new HashMap<>();
        try {
            usuarioService.resetearPassword(token, nuevaPassword);
            response.put("message", "Contraseña actualizada exitosamente.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}