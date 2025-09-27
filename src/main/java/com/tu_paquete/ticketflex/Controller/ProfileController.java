package com.tu_paquete.ticketflex.Controller;

import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<Usuario> getProfile(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuario);
    }

    @PutMapping
    public ResponseEntity<Usuario> updateProfile(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody Usuario updatedData) {
        
        // Actualizar solo los campos permitidos
        usuarioAutenticado.setNombre(updatedData.getNombre());
        usuarioAutenticado.setApellido(updatedData.getApellido());
        usuarioAutenticado.setTelefono(updatedData.getTelefono());
        usuarioAutenticado.setDireccion(updatedData.getDireccion());
        
        Usuario updatedUsuario = usuarioService.updateUsuario(usuarioAutenticado);
        return ResponseEntity.ok(updatedUsuario);
    }
}