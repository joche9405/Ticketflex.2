package com.tu_paquete.ticketflex.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Rol;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Repository.Mongo.UsuarioRepository;
import com.tu_paquete.ticketflex.dto.UsuarioDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;

import com.tu_paquete.ticketflex.Repository.Mongo.RolRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        // Verificar si ya existe un usuario con ese correo
        usuarioRepository.findByEmail(usuario.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("El usuario ya existe con ese correo electrónico");
        });

        // Buscar o crear el rol "Usuario"
        Rol rolUsuario = rolRepository.findByNombreRol("Usuario")
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombreRol("Usuario");
                    return rolRepository.save(nuevoRol);
                });

        // Asignar el rol y encriptar la contraseña
        usuario.setRol(rolUsuario);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Guardar y retornar el usuario
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario iniciarSesion(String email, String password) {
        System.out.println("Intentando iniciar sesión con email: " + email);

        return usuarioRepository.findByEmail(email)
                .map(usuario -> {
                    System.out.println("Usuario encontrado: " + usuario.getEmail());
                    boolean match = passwordEncoder.matches(password, usuario.getPassword());
                    System.out.println("Contraseña coincide? " + match);
                    if (match)
                        return usuario;
                    else
                        return null;
                })
                .orElseGet(() -> {
                    System.out.println("No se encontró usuario con ese email.");
                    return null;
                });
    }

    @Transactional(readOnly = true)
    public boolean esAdministrador(String idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(usuario -> {
                    Rol rol = usuario.getRol();
                    return rol != null && "Administrador".equalsIgnoreCase(rol.getNombreRol());
                })
                .orElse(false);
    }

    // CREACION DEL SUPER ADMIN

    @Value("${superadmin.default.email:superadmin@admin.com}")
    private String superAdminEmail;

    @Value("${superadmin.default.password:superadmin123}")
    private String superAdminPassword;

    @PostConstruct
    public void initSuperAdmin() {
        crearSuperAdminSiNoExiste();
    }

    private void crearSuperAdminSiNoExiste() {
        usuarioRepository.findByEmail(superAdminEmail).ifPresentOrElse(
                usuario -> log.info("SuperAdmin ya existe"),
                () -> {
                    Rol rolSuperAdmin = rolRepository.findByNombreRol("SuperAdmin")
                            .orElseGet(() -> {
                                Rol nuevoRol = new Rol();
                                nuevoRol.setNombreRol("SuperAdmin");
                                return rolRepository.save(nuevoRol);
                            });

                    Usuario superAdmin = new Usuario();
                    superAdmin.setNombre("Super");
                    superAdmin.setApellido("Admin");
                    superAdmin.setEmail(superAdminEmail);
                    superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
                    superAdmin.setRol(rolSuperAdmin);

                    usuarioRepository.save(superAdmin);
                    log.warn("SuperAdmin creado - Email: {}", superAdminEmail);
                });
    }

    @Transactional
    public Usuario crearAdministradorCompleto(UsuarioDTO usuarioDTO, String idSolicitante) {
        Usuario solicitante = usuarioRepository.findById(idSolicitante)
                .orElseThrow(() -> new RuntimeException("Usuario solicitante no encontrado"));

        if (!"SuperAdmin".equalsIgnoreCase(solicitante.getRol().getNombreRol())) {
            throw new RuntimeException("Solo SuperAdmin puede crear administradores");
        }

        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Rol rolAdmin = rolRepository.findByNombreRol("Administrador")
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombreRol("Administrador");
                    return rolRepository.save(nuevoRol);
                });

        Usuario nuevoAdmin = new Usuario();
        nuevoAdmin.setNombre(usuarioDTO.getNombre());
        nuevoAdmin.setApellido(usuarioDTO.getApellido());
        nuevoAdmin.setEmail(usuarioDTO.getEmail());
        nuevoAdmin.setTelefono(usuarioDTO.getTelefono());
        nuevoAdmin.setDireccion(usuarioDTO.getDireccion());
        nuevoAdmin.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        nuevoAdmin.setRol(rolAdmin);

        return usuarioRepository.save(nuevoAdmin);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarAdministradores() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != null && "Administrador".equalsIgnoreCase(u.getRol().getNombreRol()))
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());
        dto.setDireccion(usuario.getDireccion());
        return dto;
    }

    public boolean esSuperAdmin(String idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(usuario -> "SuperAdmin".equalsIgnoreCase(usuario.getRol().getNombreRol()))
                .orElse(false);
    }

    // NUEVOS METODOS

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    public Usuario updateUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario obtenerPorId(String id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public String encriptarPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public Usuario buscarPorId(String id) {
        return obtenerPorId(id);
    }

}
