package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.dto.PurchaseDTO;
import com.tu_paquete.ticketflex.repository.mongo.BoletoRepository;
import com.tu_paquete.ticketflex.repository.mongo.EventoRepository;
import com.tu_paquete.ticketflex.repository.mongo.TransaccionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Service
public class TransaccionService {
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private BoletoRepository boletoRepository;

    public Transaccion crearTransaccion(Transaccion transaccion) {
        // Validaciones adicionales si son necesarias
        if (transaccion.getTotal() == null || transaccion.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la transacción debe ser mayor que cero");
        }

        return transaccionRepository.save(transaccion);
    }

    public List<Transaccion> listarTransacciones() {
        return transaccionRepository.findAll();
    }

    // Obtiene una transacción por su id (String)
    public Transaccion obtenerTransaccionPorId(String id) {
        return transaccionRepository.findById(id).orElse(null);
    }

    // Elimina una transacción por su id (String)
    public void eliminarTransaccion(String id) {
        transaccionRepository.deleteById(id);
    }

    // Obtiene el historial de compras de un usuario dado su id (String)
    public List<Transaccion> obtenerHistorialDeCompras(String usuarioId) {
        return transaccionRepository.findByUsuarioId(usuarioId);
    }

    public Page<Transaccion> findByUsuarioId(String usuarioId, Pageable pageable) {
        List<Transaccion> lista = transaccionRepository.findByUsuarioId(usuarioId);
        return new PageImpl<>(lista, pageable, lista.size());
    }

    public List<Transaccion> filterByDateRange(String usuarioId, String filter) {
        // Por ahora, simplemente devuelve todas. Luego puedes mejorar este filtro con
        // fechas.
        return transaccionRepository.findByUsuarioId(usuarioId);
    }

    // Nuevo método para historial de compras con DTO
    public List<PurchaseDTO> obtenerHistorialDeComprasDTO(String usuarioId) {
        List<Transaccion> transacciones = transaccionRepository.findByUsuarioId(usuarioId);
        List<PurchaseDTO> historial = new ArrayList<>();

        for (Transaccion trx : transacciones) {
            System.out.println("Transacción: " + trx.getId() + " | EventoId: " + trx.getEventoId());
            Boleto boleto = null;
            Evento evento = null;
            try {
                boleto = boletoRepository.findById(trx.getBoletoId()).orElse(null);
            } catch (Exception ignored) {
            }
            try {
                evento = eventoRepository.findById(trx.getEventoId()).orElse(null);
            } catch (Exception ignored) {
            }

            // Solo agrega si hay al menos boleto o evento
            if (boleto != null && evento != null) {
                historial.add(new PurchaseDTO(trx, boleto, evento));
            } else if (boleto != null) {
                historial.add(new PurchaseDTO(trx, boleto, new Evento())); // Evento vacío
            } else if (evento != null) {
                historial.add(new PurchaseDTO(trx, new Boleto(), evento)); // Boleto vacío
            }
        }
        return historial;
    }
}
