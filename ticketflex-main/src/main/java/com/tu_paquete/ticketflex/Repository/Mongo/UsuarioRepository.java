package com.tu_paquete.ticketflex.Repository.Mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.tu_paquete.ticketflex.Model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    
    // Busca un usuario por su email
    Optional<Usuario> findByEmail(String email);

    // Comprueba si existe un usuario con ese email
    boolean existsByEmail(String email);

    // Alternativa para contar usuarios por email (útil si quieres el número en vez de boolean)
    long countByEmail(String email);
}
