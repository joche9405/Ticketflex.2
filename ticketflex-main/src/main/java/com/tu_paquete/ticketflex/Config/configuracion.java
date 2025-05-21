/*package com.tu_paquete.ticketflex.Config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Model.Rol;
import com.tu_paquete.ticketflex.Repository.Mongo.UsuarioRepository;
import com.tu_paquete.ticketflex.Repository.Mongo.RolRepository;

@Configuration
public class configuracion {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdmin() {
        try {
            // Verificar si ya existe el usuario admin
            if (usuarioRepository.findByEmail("Admin@ticketflex.com").isEmpty()) {
                // Buscar o crear el rol ADMIN
                Rol rolAdmin = rolRepository.findByNombreRol("Administrador")
                    .orElseGet(() -> {
                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombreRol("Administrador");
                        return rolRepository.save(nuevoRol);
                    });

                // Crear el usuario admin
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("Principal");
                admin.setEmail("Admin@ticketflex.com");
                admin.setPassword(passwordEncoder.encode("12345678")); // Encriptar la contraseña
                admin.setTelefono("000000000");
                admin.setDireccion("N/A");
                admin.setRol(rolAdmin);

                usuarioRepository.save(admin);
                System.out.println("Usuario administrador creado exitosamente");
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar el usuario administrador:");
            e.printStackTrace();
        }
    }
}*/