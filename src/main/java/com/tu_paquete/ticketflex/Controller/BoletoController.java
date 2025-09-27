package com.tu_paquete.ticketflex.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.Model.Boleto;

import com.tu_paquete.ticketflex.Service.BoletoService;
import com.tu_paquete.ticketflex.Service.EmailService;
import com.tu_paquete.ticketflex.Service.PayUService;
import com.tu_paquete.ticketflex.dto.PagoTarjetaRequest;

@RestController
@RequestMapping("/api/boletas")
public class BoletoController {

    @Autowired
    private BoletoService boletoService;
    @Autowired
    private PayUService payUService;
    @Autowired
    private EmailService emailService; // 👈 añade esto

    @PostMapping("/comprar")
    public ResponseEntity<?> comprarBoleto(
            @RequestParam String idEvento,
            @RequestParam String idUsuario,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) Integer cuotas,
            @RequestParam(required = false) String graderia,
            @RequestParam(required = false) String metodoPago,
            @RequestBody(required = false) PagoTarjetaRequest pagoTarjetaRequest) {

        try {
            Boleto boleto = boletoService.comprarBoleto(
                    idEvento, idUsuario, cantidad, cuotas, graderia, metodoPago, pagoTarjetaRequest);

            // ===============================
            // 🔑 Texto QR diferenciado
            // ===============================
            String qrText;
            if ("TICKETFLEX".equalsIgnoreCase(boleto.getMetodoPago())) {
                if (boleto.getEstado() == Boleto.EstadoBoleto.PENDIENTE) {
                    qrText = """
                            ⏳ RESERVA PENDIENTE - PAGO INCOMPLETO
                            Evento: %s
                            Fecha: %s
                            Gradería: %s
                            N° Boletas: %d
                            Código: %s
                            ESTADO: PENDIENTE DE PAGO
                            FECHA LÍMITE: %s
                            """.formatted(
                            boleto.getEvento().getNombre(),
                            boleto.getEvento().getFecha(),
                            boleto.getGraderia(),
                            boleto.getCantidad(),
                            boleto.getId(),
                            boleto.getFechaLimitePago());
                } else {
                    qrText = """
                            ✅ ENTRADA CONFIRMADA - ACCESO PERMITIDO
                            Evento: %s
                            Fecha: %s
                            Gradería: %s
                            N° Boletas: %d
                            Código: %s
                            ESTADO: ACTIVA - VÁLIDA PARA ENTRADA
                            """.formatted(
                            boleto.getEvento().getNombre(),
                            boleto.getEvento().getFecha(),
                            boleto.getGraderia(),
                            boleto.getCantidad(),
                            boleto.getId());
                }
            } else {
                // Pago tradicional → siempre confirmada
                qrText = """
                        ✅ ENTRADA CONFIRMADA - ACCESO PERMITIDO
                        Evento: %s
                        Fecha: %s
                        Gradería: %s
                        N° Boletas: %d
                        Código: %s
                        ESTADO: ACTIVA - VÁLIDA PARA ENTRADA
                        """.formatted(
                        boleto.getEvento().getNombre(),
                        boleto.getEvento().getFecha(),
                        boleto.getGraderia(),
                        boleto.getCantidad(),
                        boleto.getId());
            }

            // ===============================
            // URL del QR
            // ===============================
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" +
                    java.net.URLEncoder.encode(qrText, java.nio.charset.StandardCharsets.UTF_8);

            // ===============================
            // Cuerpo del correo
            // ===============================
            String subject = "🎟 Tu entrada para " + boleto.getEvento().getNombre();
            String body = """
                        <h2>¡Compra confirmada!</h2>
                        <p>Hola %s, aquí tienes los detalles de tu boleto:</p>
                        <ul>
                            <li><b>Evento:</b> %s</li>
                            <li><b>Fecha:</b> %s</li>
                            <li><b>Cantidad:</b> %d</li>
                            <li><b>Código:</b> %s</li>
                            <li><b>Estado:</b> %s</li>
                            %s
                        </ul>
                        <p><b>Escanea este QR para validar tu entrada:</b></p>
                        <div style="text-align:center;">
                            <img src="%s" alt="QR del boleto">
                        </div>
                    """.formatted(
                    boleto.getUsuario().getNombre(),
                    boleto.getEvento().getNombre(),
                    boleto.getEvento().getFecha(),
                    boleto.getCantidad(),
                    boleto.getId(),
                    boleto.getEstado(),
                    // Mostrar fecha límite si es TicketFlex pendiente
                    ("TICKETFLEX".equalsIgnoreCase(boleto.getMetodoPago())
                            && boleto.getEstado() == Boleto.EstadoBoleto.PENDIENTE)
                                    ? "<li><b>Fecha límite de pago:</b> " + boleto.getFechaLimitePago() + "</li>"
                                    : "",
                    qrUrl);

            // ===============================
            // Enviar correo
            // ===============================
            emailService.enviarCorreo(
                    boleto.getUsuario().getCorreo(),
                    subject,
                    body);

            return ResponseEntity.ok(boleto);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/pagar/{boletoId}")
    public ResponseEntity<String> pagarBoleto(@PathVariable String boletoId) {
        Boleto boleto = boletoService.obtenerPorId(boletoId); // Cambia si usas otro método
        String formularioHtml = payUService.generarFormularioRedireccion(boleto);
        return ResponseEntity.ok().body(formularioHtml);
    }

    @GetMapping("/respuesta-payu")
    public String respuestaPayU(@RequestParam(required = false) String state,
            @RequestParam(required = false) String referenceCode,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String polResponseCode,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String lapPaymentMethod,
            Model model) {

        model.addAttribute("estado", state);
        model.addAttribute("referencia", referenceCode);
        model.addAttribute("mensaje", message);
        model.addAttribute("codigoRespuesta", polResponseCode);
        model.addAttribute("metodoPago", lapPaymentMethod);
        model.addAttribute("idTransaccion", transactionId);

        return "respuesta-payu"; // Nombre de la vista HTML (por ejemplo, respuesta-payu.html)
    }

}
