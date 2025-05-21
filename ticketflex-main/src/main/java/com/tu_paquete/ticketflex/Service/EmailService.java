/*package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Mexico_City");

    /**
     * Envía confirmación de compra con TicketFlex (boleto en estado PENDIENTE)
     */
   /* public void enviarConfirmacionTicketFlex(String destinatario, Integer idBoleto, LocalDate fechaLimite, BigDecimal totalPendiente) {
        try {
            validarParametros(destinatario, idBoleto, fechaLimite, totalPendiente);
            validarFechaFutura(fechaLimite);
            
            String asunto = "✅ Confirmación de reserva con TicketFlex";
            
            Context context = new Context();
            context.setVariable("idBoleto", idBoleto);
            context.setVariable("fechaLimite", fechaLimite.format(dateFormatter));
            context.setVariable("totalPendiente", formatCurrency(totalPendiente));
            context.setVariable("diasRestantes", calcularDiasRestantes(fechaLimite));

            enviarEmailConPlantilla(destinatario, asunto, "email/confirmacion-ticketflex", context);
        } catch (Exception e) {
            logger.error("Error enviando confirmación TicketFlex a {}: {}", destinatario, e.getMessage(), e);
            throw new EmailException("Error al enviar confirmación de reserva", e);
        }
    }

    /**
     * Envía confirmación cuando se completa el pago (boleto ACTIVO)
     */
    /*public void enviarConfirmacionPagoCompleto(String destinatario, Integer idBoleto, String qrCode) {
        try {
            validarParametros(destinatario, idBoleto, qrCode);
            
            String asunto = "🎟️ ¡Tu boleto está listo! - TicketFlex";
            
            Context context = new Context();
            context.setVariable("idBoleto", idBoleto);
            context.setVariable("qrCode", qrCode);

            enviarEmailConPlantilla(destinatario, asunto, "email/pago-completo", context);
        } catch (Exception e) {
            logger.error("Error enviando confirmación de pago a {}: {}", destinatario, e.getMessage(), e);
            throw new EmailException("Error al enviar confirmación de pago", e);
        }
    }*/

    /**
     * Envía recordatorio de pago pendiente (3 días antes del vencimiento)
     */
    /*public void enviarRecordatorioPago(String destinatario, Integer idBoleto, LocalDate fechaLimite, BigDecimal saldoPendiente) {
        try {
            validarParametros(destinatario, idBoleto, fechaLimite, saldoPendiente);
            validarFechaFutura(fechaLimite);
            
            String asunto = "⏰ Recordatorio: Pago pendiente con TicketFlex";
            
            Context context = new Context();
            context.setVariable("idBoleto", idBoleto);
            context.setVariable("fechaLimite", fechaLimite.format(dateFormatter));
            context.setVariable("saldoPendiente", formatCurrency(saldoPendiente));
            context.setVariable("diasRestantes", calcularDiasRestantes(fechaLimite));

            enviarEmailConPlantilla(destinatario, asunto, "email/recordatorio-pago", context);
        } catch (Exception e) {
            logger.error("Error enviando recordatorio a {}: {}", destinatario, e.getMessage(), e);
            throw new EmailException("Error al enviar recordatorio de pago", e);
        }
    }*/

    // ========== MÉTODOS PRIVADOS DE APOYO ==========

    /*private void validarParametros(Object... parametros) {
        for (Object param : parametros) {
            if (param == null) {
                throw new IllegalArgumentException("Todos los parámetros son requeridos");
            }
            if (param instanceof String && ((String) param).trim().isEmpty()) {
                throw new IllegalArgumentException("Los parámetros String no pueden estar vacíos");
            }
        }
    }

    private void validarFechaFutura(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now(ZONA_HORARIA))) {
            throw new IllegalArgumentException("La fecha límite debe ser futura");
        }
    }

    private int calcularDiasRestantes(LocalDate fechaLimite) {
        return LocalDate.now(ZONA_HORARIA).until(fechaLimite).getDays();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("$%,.2f MXN", amount);
    }

    private void enviarEmailConPlantilla(String destinatario, String asunto, String plantilla, Context context) {
        try {
            String contenido = templateEngine.process(plantilla, context);
            enviarEmail(destinatario, asunto, contenido);
        } catch (Exception e) {
            throw new EmailException("Error al procesar plantilla: " + plantilla, e);
        }
    }

    private void enviarEmail(String destinatario, String asunto, String contenido) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido, true);
            
            mailSender.send(mensaje);
            logger.info("Email enviado exitosamente a: {}", destinatario);
        } catch (MessagingException e) {
            throw new EmailException("Error al enviar email a: " + destinatario, e);
        }
    }

    // ========== CLASE DE EXCEPCIÓN PERSONALIZADA ==========
    public static class EmailException extends RuntimeException {
        public EmailException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}*/