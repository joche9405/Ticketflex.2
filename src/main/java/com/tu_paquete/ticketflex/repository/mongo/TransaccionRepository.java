package com.tu_paquete.ticketflex.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.tu_paquete.ticketflex.Model.Transaccion;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionRepository extends MongoRepository<Transaccion, String> {

    // Encuentra todas las transacciones de un usuario dado su id (String)
    List<Transaccion> findByUsuarioId(String usuarioId);

    // encontrar por evento
    List<Transaccion> findByEventoId(String eventoId);

    // encontrar por estado de pago
    List<Transaccion> findByEstadoPago(String estadoPago);

    Optional<Transaccion> findByReferenceCode(String referenceCode);

}
