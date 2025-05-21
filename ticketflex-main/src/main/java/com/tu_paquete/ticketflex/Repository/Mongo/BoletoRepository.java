package com.tu_paquete.ticketflex.Repository.Mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.tu_paquete.ticketflex.Model.Boleto;

public interface BoletoRepository extends MongoRepository<Boleto, String> {
    // Puedes agregar métodos personalizados aquí, por ejemplo:

    // Buscar boletos por ID del usuario embebido
    List<Boleto> findByUsuario_Id(String usuarioId);

    // Buscar boletos por estado
    List<Boleto> findByEstado(Boleto.EstadoBoleto estado);

    // Buscar por QR
    Boleto findByQrCode(String qrCode);
    @Query(value = "{ 'evento.idUsuario': ?0, 'estado': ?1 }", count = true)
    Long countBoletosVendidosPorCreador(String idCreador, String estado);

}

