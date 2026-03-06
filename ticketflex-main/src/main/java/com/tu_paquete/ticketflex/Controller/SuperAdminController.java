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

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyección por constructor (Práctica recomendada sobre @Autowired en campos)
    public SuperAdminController(UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * LOGIN SUPERADMIN: Debe ser público en el SecurityConfig
     */
    @PostMapping("/login-superadmin")
    public ResponseEntity<?> loginSuperAdmin(@RequestBody LoginAdminDTO login) {
        // 1. Verificar existencia del usuario
        Usuario usuario = usuarioRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 2. Verificar contraseña
        if (!passwordEncoder.matches(login.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RespuestaAPI("Contraseña incorrecta"));
        }

        // 3. Verificar Rol específico
        if (!"SuperAdmin".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RespuestaAPI("Acceso denegado: No tiene permisos de SuperAdmin"));
        }

        // 4. Retornar ID (y el JWT se debería emitir aquí o mediante el filtro)
        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
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
            // Validación de rol delegada al servicio o interceptada aquí
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