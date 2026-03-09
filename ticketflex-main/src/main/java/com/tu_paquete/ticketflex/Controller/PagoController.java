package com.tu_paquete.ticketflex.Controller;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.dto.PagoRequest;
import com.tu_paquete.ticketflex.repository.mongo.BoletoRepository;
import com.tu_paquete.ticketflex.repository.mongo.EventoRepository;
import com.tu_paquete.ticketflex.repository.mongo.TransaccionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import java.util.Locale;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private BoletoRepository boletoRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Value("${payu.apiKey}")
    private String apiKey;

    @Value("${payu.merchantId}")
    private String merchantId;

    @Value("${payu.accountId}")
    private String accountId;

    @Value("${payu.url}")
    private String payuUrl;

    @Value("${payu.currency}")
    private String currency;

    @PostMapping("/pagar")
    public void procesarPago(@RequestBody PagoRequest pagoRequest, HttpServletResponse response) throws IOException {
        // 1. Extraer el email del usuario autenticado mediante el JWT
        String emailUsuarioLogueado = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        // 2. Buscamos el EVENTO por el ID que viene del modal
        Optional<Evento> optionalEvento = eventoRepository.findById(pagoRequest.getEventoId());

        if (optionalEvento.isEmpty()) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Evento no encontrado");
            return;
        }

        Evento evento = optionalEvento.get();

        // 3. Verificar disponibilidad básica
        if (evento.getDisponibilidad() == null || evento.getDisponibilidad() < pagoRequest.getCantidadBoletos()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "No hay suficientes entradas disponibles");
            return;
        }

        // 4. Crear registro de transacción inicial
        Transaccion transaccion = new Transaccion();
        transaccion.setEventoId(evento.getId());
        transaccion.setCantidadBoletos(pagoRequest.getCantidadBoletos());
        transaccion.setEstadoPago("PENDIENTE");

        // Calculamos el total usando 'precioBase' del modelo Evento
        BigDecimal total = evento.getPrecioBase().multiply(new BigDecimal(pagoRequest.getCantidadBoletos()));
        transaccion.setMontoPagado(total);
        transaccion.setTotal(total); // Sincronizamos con el campo 'total' de tu clase Transaccion
        transaccion.setFechaPago(new Date());

        // Seteamos los datos del comprador y tipo
        transaccion.setEmailComprador(emailUsuarioLogueado);
        transaccion.setTipo("PagoTotal");
        transaccion.setMetodoPago("TRADICIONAL");

        // Guardamos para obtener el ID generado por MongoDB
        transaccion = transaccionRepository.save(transaccion);

        // 5. Preparar parámetros para PayU
        String referencia = "TX-" + transaccion.getId();
        String montoStr = String.format(Locale.US, "%.1f", total);

        String cadenaParaFirma = apiKey + "~" + merchantId + "~" + referencia + "~" + montoStr + "~" + currency;
        String firma = DigestUtils.md5DigestAsHex(cadenaParaFirma.getBytes());

        String baseUrl = "https://ticketflex-2.onrender.com";

        // 6. Generar formulario HTML con Auto-Submit
        // Usamos getCorreo() según tu modelo de Usuario embebido en Evento
        String emailDestinoPayU = (evento.getCreador() != null && evento.getCreador().getEmail() != null)
                ? evento.getCreador().getEmail()
                : emailUsuarioLogueado; // Si el evento no tiene correo, usamos el del comprador

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head>");
        html.append("<body onload='document.forms[0].submit()'>");
        html.append("<div style='text-align:center; margin-top:50px; font-family:Arial;'>");
        html.append("  <h2>Redirigiendo a la pasarela segura de PayU...</h2>");
        html.append("  <p>Compra para: <b>").append(evento.getNombreEvento()).append("</b></p>");
        html.append("  <p>Total a pagar: <b>$").append(montoStr).append("</b></p>");
        html.append("</div>");
        html.append("<form action='").append(payuUrl).append("' method='POST'>");
        html.append("<input type='hidden' name='merchantId' value='").append(merchantId).append("'/>");
        html.append("<input type='hidden' name='accountId' value='").append(accountId).append("'/>");
        html.append("<input type='hidden' name='description' value='TicketFlex - ").append(evento.getNombreEvento())
                .append("'/>");
        html.append("<input type='hidden' name='referenceCode' value='").append(referencia).append("'/>");
        html.append("<input type='hidden' name='amount' value='").append(montoStr).append("'/>");
        html.append("<input type='hidden' name='currency' value='").append(currency).append("'/>");
        html.append("<input type='hidden' name='signature' value='").append(firma).append("'/>");
        html.append("<input type='hidden' name='buyerEmail' value='").append(emailDestinoPayU).append("'/>");
        html.append("<input type='hidden' name='test' value='1'/>");
        html.append("<input type='hidden' name='responseUrl' value='").append(baseUrl)
                .append("/api/pagos/confirmacion-payu'/>");
        html.append("<input type='submit' value='Pagar' style='display:none;'/>");
        html.append("</form></body></html>");

        response.setContentType("text/html");
        response.getWriter().write(html.toString());
    }

    @GetMapping("/confirmacion-payu")
    public ResponseEntity<String> confirmacionPayu(@RequestParam Map<String, String> allParams) {
        String referencia = allParams.get("referenceCode");
        String estadoTransaccion = allParams.get("transactionState"); // 4=Aprobado

        if (referencia == null)
            return ResponseEntity.badRequest().body("Falta referencia");

        String idTransaccion = referencia.replace("TX-", "");
        Optional<Transaccion> optTx = transaccionRepository.findById(idTransaccion);

        if (optTx.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transacción no encontrada");

        Transaccion tx = optTx.get();
        String mensajeEstado;

        if ("4".equals(estadoTransaccion)) {
            tx.setEstadoPago("PAGADO");

            Optional<Evento> optEvento = eventoRepository.findById(tx.getEventoId());
            if (optEvento.isPresent()) {
                Evento eventoOriginal = optEvento.get();

                // 1. Descontar disponibilidad en el Evento
                int nuevaDisp = eventoOriginal.getDisponibilidad() - tx.getCantidadBoletos();
                eventoOriginal.setDisponibilidad(Math.max(0, nuevaDisp));
                eventoRepository.save(eventoOriginal);

                // 2. CREAR EL BOLETO CON LAS CLASES EMBEBIDAS
                Boleto nuevoBoleto = new Boleto();

                // Llenar EventoEmbed
                Boleto.EventoEmbed evEmbed = new Boleto.EventoEmbed(
                        eventoOriginal.getId(),
                        eventoOriginal.getNombreEvento(),
                        eventoOriginal.getFecha(),
                        (eventoOriginal.getCreador() != null) ? eventoOriginal.getCreador().getId() : null);
                nuevoBoleto.setEvento(evEmbed);

                // Llenar UsuarioEmbed (Opcional: Si tienes el usuario en la Transacción)
                // Si no lo tienes, al menos inicializa el objeto para evitar
                // NullPointerException
                Boleto.UsuarioEmbed userEmbed = new Boleto.UsuarioEmbed();
                // userEmbed.setId(...);
                // userEmbed.setCorreo(allParams.get("buyerEmail")); // PayU devuelve el email
                nuevoBoleto.setUsuario(userEmbed);

                // Datos financieros
                nuevoBoleto.setPrecio(eventoOriginal.getPrecioBase());
                nuevoBoleto.setCantidad(tx.getCantidadBoletos());
                nuevoBoleto.setPrecioTotal(tx.getMontoPagado());
                nuevoBoleto.setSaldoPendiente(BigDecimal.ZERO);

                // Estados y Fechas (Usando LocalDate como pide tu modelo)
                nuevoBoleto.setFechaCompra(LocalDate.now());
                nuevoBoleto.setEstado(Boleto.EstadoBoleto.ACTIVO);
                nuevoBoleto.setMetodoPago("TRADICIONAL");
                nuevoBoleto.setGraderia("GENERAL"); // Valor por defecto o sacado de la Tx
                nuevoBoleto.setNombreBoleta(nuevoBoleto.generarNombreBoleta());

                Boleto guardado = boletoRepository.save(nuevoBoleto);
                tx.setBoletoId(guardado.getId());
            }
            mensajeEstado = "¡Pago exitoso! Tus boletos han sido generados.";

        } else if ("6".equals(estadoTransaccion)) {
            tx.setEstadoPago("RECHAZADO");
            mensajeEstado = "El pago fue rechazado.";
        } else {
            tx.setEstadoPago("PENDIENTE");
            mensajeEstado = "El pago está siendo procesado.";
        }

        transaccionRepository.save(tx);

        // Renderizado del HTML de respuesta (Igual al anterior)
        return generarHtmlRespuesta(referencia, mensajeEstado, estadoTransaccion);
    }

    private ResponseEntity<String> generarHtmlRespuesta(String referencia, String mensajeEstado,
            String estadoTransaccion) {
        String colorEstado = "4".equals(estadoTransaccion) ? "#27ae60" : "#e74c3c";

        String html = "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body{font-family:'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; text-align:center; padding-top:50px; background:#f4f7f6; color:#2d3436;}"
                +
                ".card{background:white; padding:40px; border-radius:15px; display:inline-block; box-shadow:0 10px 25px rgba(0,0,0,0.1); max-width:400px; width:90%;}"
                +
                "h1{color:#6c5ce7; margin-bottom:10px; font-size:28px;} " +
                ".status{font-size:1.2em; color:" + colorEstado
                + "; font-weight:bold; margin:20px 0; padding:10px; border-radius:8px; background:" + colorEstado
                + "15;}" +
                "p{line-height:1.6; color:#636e72;}" +
                ".ref{font-family:monospace; background:#dfe6e9; padding:5px 10px; border-radius:4px; border:1px solid #b2bec3;}"
                +
                ".btn{display:inline-block; padding:12px 25px; background:#6c5ce7; color:white; text-decoration:none; border-radius:8px; font-weight:bold; margin-top:25px; transition:background 0.3s;}"
                +
                ".btn:hover{background:#5649c1;}" +
                "</style>" +
                "</head><body>" +
                "<div class='card'>" +
                "  <h1>TicketFlex</h1>" +
                "  <div class='status'>" + mensajeEstado + "</div>" +
                "  <p>Referencia de pago:<br><span class='ref'>" + referencia + "</span></p>" +
                "  <p>Gracias por confiar en nosotros para tus eventos.</p>" +
                "  <a href='/' class='btn'>Volver a mis entradas</a>" +
                "</div>" +
                "</body></html>";

        return ResponseEntity.ok().contentType(org.springframework.http.MediaType.TEXT_HTML).body(html);
    }
}