package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Boleto.EventoEmbed;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.PagoCuota;
import com.tu_paquete.ticketflex.dto.CompraCuotasRequest;
import com.tu_paquete.ticketflex.dto.DetalleCompraCuotasDTO;
import com.tu_paquete.ticketflex.repository.mongo.BoletoRepository;
import com.tu_paquete.ticketflex.repository.mongo.EventoRepository;
import com.tu_paquete.ticketflex.repository.mongo.PagoCuotaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PagoCuotaServiceImpl implements PagoCuotaService {

    @Autowired
    private BoletoRepository boletoRepository;

    @Autowired
    private PagoCuotaRepository pagoCuotaRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Override
    public List<PagoCuota> obtenerTodos() {
        return pagoCuotaRepository.findAll();
    }

    @Override
    public PagoCuota obtenerPorId(String id) {
        return pagoCuotaRepository.findById(id).orElse(null);
    }

    @Override
    public PagoCuota guardarPagoCuota(PagoCuota cuota) {
        return pagoCuotaRepository.save(cuota);
    }

    @Override
    public PagoCuota eliminarPorId(String id) {
        PagoCuota cuota = pagoCuotaRepository.findById(id).orElse(null);
        if (cuota != null) {
            pagoCuotaRepository.deleteById(id);
        }
        return cuota;
    }

    @Override
    public void liberarBoletasNoPagadas() {
        LocalDate hoy = LocalDate.now();

        List<PagoCuota> cuotasVencidas = pagoCuotaRepository
                .findByPagadoFalseAndFechaVencimientoBefore(hoy.minusDays(5));

        for (PagoCuota cuota : cuotasVencidas) {
            String idBoleto = cuota.getIdBoleto();
            if (idBoleto != null) {
                Optional<Boleto> boletoOpt = boletoRepository.findById(idBoleto);
                if (boletoOpt.isPresent()) {
                    Boleto boleto = boletoOpt.get();
                    boleto.setEstado(Boleto.EstadoBoleto.ACTIVO);
                    boleto.setUsuario(null);
                    boletoRepository.save(boleto);
                }
            }
        }
    }

    @Override
    public List<PagoCuota> obtenerCuotasPorUsuario(String idUsuario) {
        return pagoCuotaRepository.findByUsuarioId(idUsuario);
    }

    @Override
    public List<PagoCuota> generarCuotasDeCompra(CompraCuotasRequest request, LocalDate fechaEvento) {
        List<DetalleCompraCuotasDTO> detalles = request.getDetalles();

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una boleta.");
        }

        int totalBoletas = detalles.stream()
                .mapToInt(DetalleCompraCuotasDTO::getCantidad)
                .sum();
        if (totalBoletas > 6) {
            throw new IllegalArgumentException("No se pueden comprar más de 6 boletas a crédito.");
        }

        LocalDate hoy = LocalDate.now();

        long mesesDisponibles = ChronoUnit.MONTHS.between(
                hoy.withDayOfMonth(1),
                fechaEvento.withDayOfMonth(1));

        if (mesesDisponibles < 2) {
            throw new IllegalArgumentException("No se puede pagar por cuotas si el evento ocurre en 1 mes o menos.");
        }

        int cuotasPermitidas = (int) Math.min(6, mesesDisponibles - 1);
        List<PagoCuota> cuotasGeneradas = new ArrayList<>();

        for (DetalleCompraCuotasDTO detalle : detalles) {
            Optional<Evento> eventoOpt = eventoRepository.findById(detalle.getIdEvento());
            if (eventoOpt.isEmpty()) {
                throw new IllegalArgumentException("Evento no encontrado con ID: " + detalle.getIdEvento());
            }

            Evento evento = eventoOpt.get();

            int cantidadSolicitada = detalle.getCantidad();

            if (evento.getDisponibilidad() < cantidadSolicitada) {
                throw new IllegalArgumentException(
                        "No hay suficientes boletas disponibles para el evento: " + evento.getNombreEvento());
            }

            // Restar la cantidad de boletas compradas
            evento.setDisponibilidad(evento.getDisponibilidad() - cantidadSolicitada);
            eventoRepository.save(evento);

            // Por cada boleta solicitada, crear boleto y cuotas correspondientes
            for (int i = 0; i < cantidadSolicitada; i++) {
                // Crear boleto
                Boleto boleto = new Boleto();
                String idBoleto = UUID.randomUUID().toString();
                boleto.setId(idBoleto);
                boleto.setPrecio(detalle.getValor()); // valor por boleto
                boleto.setEstado(Boleto.EstadoBoleto.PENDIENTE);

                Boleto.UsuarioEmbed user = new Boleto.UsuarioEmbed();
                user.setId(detalle.getIdUsuario());
                boleto.setUsuario(user);

                EventoEmbed eventoEmbed = new EventoEmbed();
                eventoEmbed.setId(evento.getId().toString());
                eventoEmbed.setNombre(evento.getNombreEvento());
                // Si hay más campos en EventoEmbed, asignarlos aquí
                boleto.setEvento(eventoEmbed);

                boletoRepository.save(boleto);

                // Calcular cuotas para ese boleto
                BigDecimal valorTotal = detalle.getValor();
                BigDecimal valorPorCuota = valorTotal.divide(BigDecimal.valueOf(cuotasPermitidas), 2,
                        RoundingMode.HALF_UP);
                String idCompra = UUID.randomUUID().toString();

                for (int cuotaNum = 0; cuotaNum < cuotasPermitidas; cuotaNum++) {
                    PagoCuota cuota = new PagoCuota();
                    cuota.setIdCompra(idCompra);
                    cuota.setIdBoleto(idBoleto);
                    cuota.setUsuarioId(detalle.getIdUsuario());
                    cuota.setNumeroCuota(cuotaNum + 1);
                    cuota.setValor(valorPorCuota);
                    cuota.setPagado(false);
                    cuota.setFechaVencimiento(hoy.plusMonths(cuotaNum + 1));
                    cuota.setEstado("PENDIENTE");
                    pagoCuotaRepository.save(cuota);
                    cuotasGeneradas.add(cuota);
                }
            }
        }

        return cuotasGeneradas;
    }

    @Override
    public boolean marcarComoPagada(String idCuota) {
        Optional<PagoCuota> cuotaOpt = pagoCuotaRepository.findById(idCuota);
        if (cuotaOpt.isPresent()) {
            PagoCuota cuota = cuotaOpt.get();
            cuota.setPagado(true);
            cuota.setFechaPago(LocalDate.now());
            cuota.setEstado("PAGADA");
            pagoCuotaRepository.save(cuota);
            return true;
        }
        return false;
    }

    @Override
    public void actualizarEstadosCuotasVencidas() {
        List<PagoCuota> todasLasCuotas = pagoCuotaRepository.findAll();
        LocalDate hoy = LocalDate.now();

        for (PagoCuota cuota : todasLasCuotas) {
            if (!cuota.isPagado()) {
                if (cuota.getFechaVencimiento().isBefore(hoy)) {
                    cuota.setEstado("VENCIDA");
                } else {
                    cuota.setEstado("PENDIENTE");
                }
                pagoCuotaRepository.save(cuota);
            }
        }
    }

    @Override
    public List<PagoCuota> obtenerCuotasPorBoleto(String idBoleto) {
        return pagoCuotaRepository.findByIdBoleto(idBoleto);
    }

}
