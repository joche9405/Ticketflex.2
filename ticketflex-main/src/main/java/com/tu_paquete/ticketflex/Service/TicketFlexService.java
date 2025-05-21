/*package com.tu_paquete.ticketflex.Service;

import com.tu_paquete.ticketflex.PagoRequest;
import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Boleto.EstadoBoleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Repository.BoletoRepository;
import com.tu_paquete.ticketflex.Repository.EventoRepository;
import com.tu_paquete.ticketflex.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TicketFlexService {

    @Autowired
    private BoletoRepository boletoRepository;
    
    @Autowired
    private EventoRepository eventoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EmailService emailService;

    private static final BigDecimal DESCUENTO_TICKETFLEX = new BigDecimal("0.9"); // 10% de descuento

    @Transactional
    public Map<String, Object> comprarConTicketFlex(Integer idEvento, Integer idUsuario, Integer cantidad) {
        // Validar y obtener datos
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if(evento.getDisponibilidad() < cantidad) {
            throw new RuntimeException("No hay suficientes boletos disponibles");
        }

        // Crear boleto con estado PENDIENTE
        Boleto boleto = new Boleto();
        boleto.setEvento(evento);
        boleto.setUsuario(usuario);
        boleto.setCantidad(cantidad);
        
        // Aplicar descuento del 10%
        BigDecimal precioConDescuento = evento.getPrecioBase().multiply(DESCUENTO_TICKETFLEX);
        boleto.setPrecio(precioConDescuento);
        boleto.setPrecioTotal(precioConDescuento.multiply(new BigDecimal(cantidad)));
        boleto.setEstado(EstadoBoleto.PENDIENTE);
        boleto.setFechaLimitePago(LocalDate.now().plusMonths(1)); // 1 mes para pagar
        boleto.setFechaCompra(new java.util.Date());
        boleto.setQrCode("PENDIENTE|" + evento.getIdEvento() + "|" + usuario.getIdUsuario()); // QR temporal

        // Guardar y actualizar disponibilidad
        Boleto boletoGuardado = boletoRepository.save(boleto);
        evento.setDisponibilidad(evento.getDisponibilidad() - cantidad);
        eventoRepository.save(evento);

        // Enviar email de confirmación
        emailService.enviarConfirmacionTicketFlex(
            usuario.getEmail(),
            boletoGuardado.getIdBoleto(),
            boletoGuardado.getFechaLimitePago(),
            boletoGuardado.getPrecioTotal()
        );

        // Preparar respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("idBoleto", boletoGuardado.getIdBoleto());
        response.put("estado", boletoGuardado.getEstado().name());
        response.put("fechaLimitePago", boletoGuardado.getFechaLimitePago());
        response.put("qrTemporal", boletoGuardado.getQrCode());
        response.put("precioTotal", boletoGuardado.getPrecioTotal());
        response.put("saldoPendiente", boletoGuardado.getPrecioTotal()); // En este caso el saldo pendiente es el total

        return response;
    }

    @Transactional
    public Boleto completarPagoTicketFlex(Integer idBoleto, PagoRequest pagoRequest) {
        // Buscar el boleto
        Boleto boleto = boletoRepository.findById(idBoleto)
                .orElseThrow(() -> new RuntimeException("Boleto no encontrado"));
        
        // Validar estado
        if(boleto.getEstado() != EstadoBoleto.PENDIENTE) {
            throw new RuntimeException("El boleto no está pendiente de pago. Estado actual: " + boleto.getEstado());
        }
        
        // Validar fecha límite
        if(LocalDate.now().isAfter(boleto.getFechaLimitePago())) {
            boleto.setEstado(EstadoBoleto.CANCELADO);
            boletoRepository.save(boleto);
            throw new RuntimeException("La fecha límite de pago ha expirado");
        }

        // Aquí iría la integración con la pasarela de pago (Stripe, PayPal, etc.)
        boolean pagoExitoso = procesarPagoConPasarela(pagoRequest);
        
        if(!pagoExitoso) {
            throw new RuntimeException("Error al procesar el pago");
        }

        // Actualizar boleto
        boleto.setEstado(EstadoBoleto.ACTIVO);
        boleto.setQrCode("CONFIRMADO|" + boleto.getIdBoleto()); // QR definitivo
        Boleto boletoActualizado = boletoRepository.save(boleto);
        
        // Enviar email con QR definitivo
        emailService.enviarConfirmacionPagoCompleto(
            boleto.getUsuario().getEmail(),
            boleto.getIdBoleto(),
            boleto.getQrCode()
        );
        
        return boletoActualizado;
    }

    private boolean procesarPagoConPasarela(PagoRequest pagoRequest) {
        // Implementación simulada - en producción usarías Stripe, PayPal, etc.
        return pagoRequest.getNumeroTarjeta() != null && 
               !pagoRequest.getNumeroTarjeta().isEmpty();
    }
    @Transactional
    public boolean completarPago(PagoRequest pagoRequest) {
        if (pagoRequest.getIdBoleto() == null) {
            throw new RuntimeException("ID de boleto es requerido");
        }
        
        Boleto boleto = completarPagoTicketFlex(pagoRequest.getIdBoleto(), pagoRequest);
        return boleto != null && boleto.getEstado() == EstadoBoleto.ACTIVO;
    }
}*/