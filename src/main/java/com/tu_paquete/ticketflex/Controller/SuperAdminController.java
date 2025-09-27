package com.tu_paquete.ticketflex.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public SuperAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

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
                    .body(new RespuestaAPI("Administrador creado exitosamente"));

        } catch (RuntimeException e) {
            // Maneja tanto SecurityException como otras excepciones de negocio
            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/administradores")
    public ResponseEntity<?> listarAdministradores(
            @RequestHeader("idUsuario") String idSolicitante) {

        try {
            if (!usuarioService.esSuperAdmin(idSolicitante)) {
                throw new RuntimeException("Acceso no autorizado: Se requiere rol SuperAdmin");
            }

            return ResponseEntity.ok(usuarioService.listarAdministradores()); // <--- aquí estaba el error

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RespuestaAPI(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RespuestaAPI("Error interno: " + e.getMessage()));
        }
    }

    @PostMapping("/login-superadmin")
    public ResponseEntity<?> loginSuperAdmin(@RequestBody LoginAdminDTO login) {
        Usuario usuario = usuarioRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(login.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Contraseña incorrecta");
        }

        if (!"SuperAdmin".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permisos de SuperAdmin");
        }

        return ResponseEntity.ok(Map.of("id", usuario.getId())); // devuelve el ID para guardarlo
    }

}