package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.dto.PagoTarjetaRequest;
import com.tu_paquete.ticketflex.repository.mongo.BoletoRepository;
import com.tu_paquete.ticketflex.repository.mongo.EventoRepository;
import com.tu_paquete.ticketflex.repository.mongo.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class BoletoService {

    @Autowired
    private BoletoRepository boletoRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransaccionService transaccionService;

    public Boleto obtenerPorId(String id) {
        return boletoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boleto no encontrado"));
    }

    // ✅ MÉTODO SOBRECARGADO PARA COMPATIBILIDAD
    public Boleto comprarBoleto(String idEvento, String idUsuario, Integer cantidad) {
        return comprarBoleto(idEvento, idUsuario, cantidad, 1, "general", "TRADICIONAL", null);
    }

    // ✅ MÉTODO COMPLETO ACTUALIZADO PARA TICKETFLEX Y PAGO TRADICIONAL
    public Boleto comprarBoleto(String idEvento, String idUsuario, Integer cantidad,
            Integer cuotas, String graderia, String metodoPago, PagoTarjetaRequest pagoTarjetaRequest) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (evento.getDisponibilidad() < cantidad) {
            throw new RuntimeException("No hay suficiente disponibilidad para este evento");
        }

        // Validar datos de tarjeta si es pago tradicional
        if ("TRADICIONAL".equalsIgnoreCase(metodoPago) && pagoTarjetaRequest != null) {
            // Validar datos de la tarjeta
            if (!validarTarjeta(pagoTarjetaRequest)) {
                throw new RuntimeException("Datos de tarjeta inválidos");
            }

            // Procesar pago con tarjeta (simulación)
            boolean pagoExitoso = procesarPagoTarjeta(pagoTarjetaRequest,
                    evento.getPrecioBase().multiply(BigDecimal.valueOf(cantidad)));

            if (!pagoExitoso) {
                throw new RuntimeException("Pago con tarjeta rechazado");
            }
        }

        Boleto boleto = new Boleto();

        // Configurar evento embed
        Boleto.EventoEmbed evEmb = new Boleto.EventoEmbed();
        evEmb.setId(evento.getId().toString());
        evEmb.setNombre(evento.getNombreEvento());

        // ✅ MANEJO DE FECHA DEL EVENTO (si existe)
        if (evento.getFecha() != null) {
            try {
                // Si el evento tiene fecha, intentar asignarla
                evEmb.setFecha(evento.getFecha());
            } catch (Exception e) {
                System.out.println("Error al asignar fecha del evento: " + e.getMessage());
            }
        }

        boleto.setEvento(evEmb);

        // Configurar usuario embed
        Boleto.UsuarioEmbed uEmb = new Boleto.UsuarioEmbed();
        uEmb.setId(usuario.getId());
        uEmb.setNombre(usuario.getNombre());
        uEmb.setCorreo(usuario.getEmail());
        boleto.setUsuario(uEmb);

        // Campos básicos
        boleto.setFechaCompra(java.time.LocalDate.now());
        boleto.setCantidad(cantidad);
        boleto.setPrecio(evento.getPrecioBase());
        boleto.setPrecioTotal(evento.getPrecioBase().multiply(BigDecimal.valueOf(cantidad)));

        // ✅ CAMPOS NUEVOS PARA TICKETFLEX
        boleto.setGraderia(graderia != null ? graderia : "general");
        boleto.setCuotas(cuotas != null ? cuotas : 1);
        boleto.setMetodoPago(metodoPago != null ? metodoPago : "TRADICIONAL");
        boleto.setNombreBoleta(generarNombreBoleta(evento.getNombreEvento(), graderia));

        // ✅ Calcular fecha límite y próxima fecha de pago para TicketFlex
        if ("TICKETFLEX".equalsIgnoreCase(metodoPago)) {
            boleto.setFechaLimitePago(LocalDate.now().plusMonths(cuotas));
            boleto.setEstado(Boleto.EstadoBoleto.PENDIENTE);

            // Próximo pago siempre es un mes después de la compra
            boleto.setFechaProximoPago(LocalDate.now().plusMonths(1));
        } else {
            boleto.setEstado(Boleto.EstadoBoleto.ACTIVO);
            boleto.setFechaProximoPago(null); // no aplica para pago único
        }

        Boleto boletoGuardado = boletoRepository.save(boleto);

        // Actualizar disponibilidad del evento
        evento.setDisponibilidad(evento.getDisponibilidad() - cantidad);
        eventoRepository.save(evento);

        // Crear transacción
        Transaccion trx = new Transaccion();
        trx.setBoletoId(boletoGuardado.getId());
        trx.setUsuarioId(usuario.getId());
        trx.setEventoId(evento.getId().toString());
        trx.setCantidadBoletos(cantidad);
        trx.setTotal(boletoGuardado.getPrecioTotal());
        trx.setFechaPago(new Date());

        // ✅ Estado de transacción según método de pago
        if ("TICKETFLEX".equalsIgnoreCase(metodoPago)) {
            trx.setEstadoPago("PENDIENTE");
        } else {
            trx.setEstadoPago("COMPLETADO");
        }

        transaccionService.crearTransaccion(trx);

        return boletoGuardado;
    }

    // ✅ MÉTODO PARA VALIDAR DATOS DE TARJETA (modificado para debug)
    // ✅ MÉTODO PARA VALIDAR DATOS DE TARJETA (ajustado)
    private boolean validarTarjeta(PagoTarjetaRequest tarjeta) {
        // Validaciones básicas de tarjeta (más flexibles)
        if (tarjeta.getCardNumber() == null || tarjeta.getCardNumber().length() < 10) {
            System.out.println("Error: Número de tarjeta inválido (mínimo 10 dígitos)");
            return false;
        }
        if (tarjeta.getExpiryDate() == null || !tarjeta.getExpiryDate().matches("\\d{2}/\\d{2}")) {
            System.out.println("Error: Fecha de expiración inválida. Use formato MM/AA");
            return false;
        }
        if (tarjeta.getCvv() == null || tarjeta.getCvv().length() < 3 || tarjeta.getCvv().length() > 4) {
            System.out.println("Error: CVV inválido (debe tener 3 o 4 dígitos)");
            return false;
        }

        System.out.println("Tarjeta validada correctamente");
        return true;
    }

    // ✅ MÉTODO PARA PROCESAR PAGO CON TARJETA
    private boolean procesarPagoTarjeta(PagoTarjetaRequest tarjeta, BigDecimal monto) {
        // Aquí integrarías con tu pasarela de pago (PayU, Stripe, etc.)
        // Por ahora, simulamos un pago exitoso
        try {
            // Lógica de integración con pasarela de pago
            System.out.println("Procesando pago con tarjeta: " + tarjeta.getCardNumber());
            System.out.println("Monto: " + monto);
            // Simular procesamiento...
            Thread.sleep(1000); // Simular delay de procesamiento

            return true; // Pago exitoso
        } catch (Exception e) {
            System.err.println("Error en procesamiento de tarjeta: " + e.getMessage());
            return false; // Pago fallido
        }
    }

    // ✅ MÉTODO AUXILIAR PARA GENERAR NOMBRE DE BOLETA
    private String generarNombreBoleta(String nombreEvento, String graderia) {
        if (nombreEvento != null && graderia != null) {
            return nombreEvento + " - " + graderia.toUpperCase();
        }
        return "Boleto General";
    }

    public List<Boleto> findByUsuarioId(String usuarioId) {
        return boletoRepository.findByUsuario_Id(usuarioId);
    }

    // ✅ MÉTODOS ADICIONALES ÚTILES
    public List<Boleto> findByEventoId(String eventoId) {
        return boletoRepository.findByEvento_Id(eventoId);
    }

    public List<Boleto> findByEstado(Boleto.EstadoBoleto estado) {
        return boletoRepository.findByEstado(estado);
    }

    public List<Boleto> findByMetodoPago(String metodoPago) {
        return boletoRepository.findByMetodoPago(metodoPago);
    }

    // ✅ MÉTODO PARA ACTUALIZAR ESTADO DE BOLETO
    public Boleto actualizarEstado(String boletoId, Boleto.EstadoBoleto nuevoEstado) {
        Boleto boleto = obtenerPorId(boletoId);
        boleto.setEstado(nuevoEstado);
        return boletoRepository.save(boleto);
    }

    // ✅ MÉTODO PARA MARCAR BOLETO COMO PAGADO
    public Boleto marcarComoPagado(String boletoId) {
        Boleto boleto = obtenerPorId(boletoId);
        boleto.setEstado(Boleto.EstadoBoleto.ACTIVO);
        boleto.setFechaLimitePago(null); // Eliminar fecha límite una vez pagado
        return boletoRepository.save(boleto);
    }
}