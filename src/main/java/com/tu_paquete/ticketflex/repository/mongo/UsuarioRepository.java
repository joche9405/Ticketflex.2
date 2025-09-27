package com.tu_paquete.ticketflex.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tu_paquete.ticketflex.Model.Rol;
import com.tu_paquete.ticketflex.Model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    // Busca un usuario por su email
    Optional<Usuario> findByEmail(String email);

    // Comprueba si existe un usuario con ese email
    boolean existsByEmail(String email);

    List<Usuario> findByRol(Rol rol);

    // Alternativa para contar usuarios por email (útil si quieres el número en vez
    // de boolean)
    long countByEmail(String email);

    // NUEVA LÓGICA: Buscar un usuario por su token de reseteo
    Optional<Usuario> findByResetPasswordToken(String resetPasswordToken);
}
