package com.tu_paquete.ticketflex.Repository.Mongo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.tu_paquete.ticketflex.Model.Transaccion;
import java.util.List;
import java.util.Optional;

public interface TransaccionRepository extends MongoRepository<Transaccion, ObjectId> {
    
    // Encuentra todas las transacciones de un usuario dado su id (String)
    List<Transaccion> findByUsuarioId(String usuarioId);
    
    // Opcional: encontrar por evento
    List<Transaccion> findByEventoId(String eventoId);
    
    // Opcional: encontrar por estado de pago
    List<Transaccion> findByEstadoPago(String estadoPago);
    Optional<Transaccion> findByReferenceCode(String referenceCode);

}
