package com.tu_paquete.ticketflex.Controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.UsuarioService;
import com.tu_paquete.ticketflex.dto.LoginAdminDTO;
import com.tu_paquete.ticketflex.dto.RespuestaAPI;
import com.tu_paquete.ticketflex.dto.UsuarioDTO;
import com.tu_paquete.ticketflex.repository.mongo.UsuarioRepository;
import com.tu_paquete.ticketflex.segurity.JwtUtil;

import jakarta.servlet.http.Cookie; // Importante: faltaba esta importación
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Constructor CORREGIDO (sintaxis y comas)
    public SuperAdminController(UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * LOGIN SUPERADMIN: Verifica credenciales y emite Cookie con JWT
     */
    @PostMapping("/login-superadmin")
    public ResponseEntity<?> loginSuperAdmin(@RequestBody LoginAdminDTO login, HttpServletResponse response) {
        // 1. Verificar existencia del usuario
        Usuario usuario = usuarioRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 2. Verificar contraseña
        if (!passwordEncoder.matches(login.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RespuestaAPI("Contraseña incorrecta"));
        }

        // 3. Verificar Rol específico (Ignora mayúsculas/minúsculas)
        String nombreRol = usuario.getRol().getNombreRol();
        if (!"SuperAdmin".equalsIgnoreCase(nombreRol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RespuestaAPI("Acceso denegado: No tiene permisos de SuperAdmin"));
        }

        // 4. GENERAR TOKEN JWT
        String token = jwtUtil.generarToken(usuario.getId(), nombreRol);

        // 5. CREAR COOKIE SEGURA
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Necesario para HTTPS en Render
        cookie.setPath("/");
        cookie.setMaxAge(3600); // 1 hora de duración
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
                "rol", nombreRol,
                "token", token,
                "mensaje", "Login exitoso como SuperAdmin"));
    }

    /**
     * CREAR ADMINISTRADORES: Solo accesible por SuperAdmin
     */
    @PostMapping("/administradores")
    public ResponseEntity<?> crearAdministradorCompleto(
            @RequestBody UsuarioDTO usuarioDTO,
            @RequestHeader("idUsuario") String idSolicitante) {

        try {
            Usuario nuevoAdmin = usuarioService.crearAdministradorCompleto(usuarioDTO, idSolicitante);

            return ResponseEntity.created(
                    ServletUriComponentsBuilder.fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(nuevoAdmin.getId())
                            .toUri())
                    .body(new RespuestaAPI("Administrador creado exitosamente: " + nuevoAdmin.getNombre()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new RespuestaAPI(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new RespuestaAPI(e.getMessage()));
        }
    }

    /**
     * LISTAR ADMINISTRADORES: Solo accesible por SuperAdmin
     */
    @GetMapping("/administradores")
    public ResponseEntity<?> listarAdministradores(
            @RequestHeader("idUsuario") String idSolicitante) {

        try {
            if (!usuarioService.esSuperAdmin(idSolicitante)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new RespuestaAPI("Acceso no autorizado: Se requiere rol SuperAdmin"));
            }

            return ResponseEntity.ok(usuarioService.listarAdministradores());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RespuestaAPI("Error al obtener la lista: " + e.getMessage()));
        }
    }

}