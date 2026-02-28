package com.tu_paquete.ticketflex.repository.mongo;

import com.tu_paquete.ticketflex.Model.PagoCuota;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagoCuotaRepository extends MongoRepository<PagoCuota, String> {

    // Buscar todas las cuotas de un boleto específico
    List<PagoCuota> findByIdBoleto(String idBoleto);

    // Buscar todas las cuotas de un usuario específico (por el id del usuario
    // referenciado)
    List<PagoCuota> findByUsuarioId(String usuarioId);

    // Buscar cuotas según su estado de pago
    List<PagoCuota> findByPagado(boolean pagado);

    // Buscar cuotas no pagadas con fecha de vencimiento anterior a la fecha dada
    List<PagoCuota> findByPagadoFalseAndFechaVencimientoBefore(LocalDate fecha);
}
