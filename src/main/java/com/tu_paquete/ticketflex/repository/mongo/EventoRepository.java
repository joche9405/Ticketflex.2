package com.tu_paquete.ticketflex.repository.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.dto.EventoConEstadisticas;

public interface EventoRepository extends MongoRepository<Evento, String> {

        List<Evento> findByLugar(String lugar);

        List<Evento> findByFecha(LocalDate fecha);

        List<Evento> findByCategoria(String categoria);

        List<Evento> findByArtista(String artista);

        List<Evento> findByLugarAndFechaAndCategoriaAndArtista(String lugar, LocalDate fecha, String categoria,
                        String artista);

        // En MongoDB usamos el ID como String
        List<Evento> findByCreadorId(String idUsuario);

        // Versión paginada
        Page<Evento> findByCreadorId(String idUsuario, Pageable pageable);

        // Consulta con estadísticas usando agregación
        @Aggregation(pipeline = {
                        "{ $match: { 'creador._id': ?0 } }", // Cambiado a creador._id
                        "{ $lookup: { " +
                                        "from: 'boletos', " +
                                        "let: { eventoId: '$_id' }, " +
                                        "pipeline: [ " +
                                        "{ $match: { " +
                                        "$expr: { $eq: ['$evento._id', '$$eventoId'] } " + // Cambiado a evento._id
                                        "} } " +
                                        "], " +
                                        "as: 'boletos' " +
                                        "} }",
                        "{ $addFields: { " +
                                        "boletosVendidos: { $size: '$boletos' }, " + // Todos los boletos se consideran
                                                                                     // vendidos
                                        "ingresos: { " +
                                        "$sum: { " +
                                        "$map: { " +
                                        "input: '$boletos', " +
                                        "as: 'boleto', " +
                                        "in: { $toDouble: '$$boleto.precio' } " + // Convertir String a Double
                                        "} " +
                                        "} " +
                                        "} " +
                                        "} }",
                        "{ $project: { " +
                                        "idEvento: '$_id', " +
                                        "nombreEvento: 1, " +
                                        "fecha: 1, " +
                                        "capacidad: '$disponibilidad', " + // Cambiado a disponibilidad
                                        "boletosVendidos: 1, " +
                                        "ingresos: 1 " +
                                        "categoria: 1, " +
                                        "artista: 1 " +
                                        "} }"
        })
        List<EventoConEstadisticas> findEventosConEstadisticas(String idCreador);

        @Query("{ '_id' : ?0 }")
        Optional<Evento> findByIdWithLock(String id);
}