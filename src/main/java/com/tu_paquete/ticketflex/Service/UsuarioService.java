package com.tu_paquete.ticketflex.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Rol;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.dto.UsuarioDTO;
import com.tu_paquete.ticketflex.repository.mongo.RolRepository;
import com.tu_paquete.ticketflex.repository.mongo.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Inyección de dependencia del nuevo servicio

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        usuarioRepository.findByEmail(usuario.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("El usuario ya existe con ese correo electrónico");
        });

        Rol rolUsuario = rolRepository.findByNombreRol("Usuario")
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombreRol("Usuario");
                    return rolRepository.save(nuevoRol);
                });

        usuario.setRol(rolUsuario);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

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
        // 1. Encuentra el objeto Rol 'Administrador' por su nombre.
        Rol rolAdmin = rolRepository.findByNombreRol("Administrador")
                .orElseThrow(() -> new RuntimeException("Rol 'Administrador' no encontrado."));

        // 2. Usa el objeto Rol para buscar usuarios asociados a ese ID de rol.
        List<Usuario> administradores = usuarioRepository.findByRol(rolAdmin);

        // 3. Mapea la lista a DTOs.
        return administradores.stream()
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

    // NUEVA LÓGICA: Métodos para restablecer contraseña

    @Transactional
    public String generarTokenDeReseteo(String email, boolean esAdmin) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElse(null);

        if (usuario == null) {
            log.info("Usuario con email {} no encontrado. No se enviará correo.", email);
            return null;
        }

        // Revisar si ya existe un token activo
        if (usuario.getResetPasswordToken() != null &&
                usuario.getResetPasswordTokenExpiry() != null &&
                usuario.getResetPasswordTokenExpiry().isAfter(LocalDateTime.now())) {

            log.info("Usuario {} ya tiene un token activo que expira en {}. No se enviará nuevo correo.",
                    email, usuario.getResetPasswordTokenExpiry());
            return usuario.getResetPasswordToken();
        }

        // Limpiar token expirado
        if (usuario.getResetPasswordToken() != null &&
                usuario.getResetPasswordTokenExpiry() != null &&
                usuario.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {

            log.info("Token expirado para usuario {}. Generando nuevo token.", email);
            usuario.setResetPasswordToken(null);
            usuario.setResetPasswordTokenExpiry(null);
        }

        // Generar nuevo token
        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(10));
        usuarioRepository.save(usuario);

        // Construir link según tipo de usuario
        String resetLink;
        if (esAdmin) {
            resetLink = "https://ticketflex-2.onrender.com/admin/reset-password/" + token;
        } else {
            resetLink = "https://ticketflex-2.onrender.com//reset-password/" + token;
        }

        // Preparar y enviar correo
        String subject = "Restablece tu contraseña - TicketFlex";
        String body = "<p>Hola,</p>"
                + "<p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta TicketFlex.</p>"
                + "<p>Por favor, haz clic en el siguiente enlace para crear una nueva contraseña:</p>"
                + "<p><a href=\"" + resetLink + "\">Restablecer Contraseña</a></p>"
                + "<p>Este enlace es válido por 10 minutos. Si no solicitaste este cambio, puedes ignorar este correo.</p>"
                + "<p>Gracias,<br>Soporte de TicketFlex</p>";

        emailService.enviarCorreo(email, subject, body);
        log.info("Correo de reseteo enviado exitosamente a: {}. Token expira en 10 minutos.", email);

        return token;
    }

    @Transactional
    public void resetearPassword(String token, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de restablecimiento inválido."));

        if (usuario.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token ha expirado. Por favor, solicita uno nuevo.");
        }

        // Encriptar la nueva contraseña
        String passwordEncriptada = passwordEncoder.encode(nuevaPassword);
        usuario.setPassword(passwordEncriptada);

        // Invalidar el token para evitar que se use de nuevo
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiry(null);

        usuarioRepository.save(usuario);
    }

}