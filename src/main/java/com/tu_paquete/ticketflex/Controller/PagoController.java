package com.tu_paquete.ticketflex.Controller;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.dto.PagoRequest;
import com.tu_paquete.ticketflex.repository.mongo.BoletoRepository;
import com.tu_paquete.ticketflex.repository.mongo.TransaccionRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

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
        Optional<Boleto> optionalBoleto = boletoRepository.findById(pagoRequest.getIdBoleto());
        if (!optionalBoleto.isPresent()) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Boleto no encontrado");
            return;
        }

        Boleto boleto = optionalBoleto.get();

        pagoRequest.setUsuarioId(boleto.getUsuario().getId());
        pagoRequest.setEventoId(boleto.getEvento().getId());
        pagoRequest.setCantidadBoletos(boleto.getCantidad());
        pagoRequest.setTotal(boleto.getPrecioTotal());

        Transaccion transaccion = new Transaccion();
        transaccion.setBoletoId(boleto.getId());
        transaccion.setEstadoPago("PENDIENTE");
        transaccion.setMetodoPago(pagoRequest.getMetodoPago());
        transaccion.setMontoPagado(pagoRequest.getTotal());
        transaccion.setFechaPago(new Date());
        transaccion = transaccionRepository.save(transaccion);

        String referencia = "TX-" + transaccion.getId();
        String montoStr = String.format("%.2f", pagoRequest.getTotal());
        String cadenaParaFirma = apiKey + "~" + merchantId + "~" + referencia + "~" + montoStr + "~" + currency;
        String firma = DigestUtils.md5DigestAsHex(cadenaParaFirma.getBytes());

        String formularioPayU = "<html><body onload='document.forms[0].submit()'>" +
                "<form action='" + payuUrl + "' method='POST'>" +
                "<input type='hidden' name='merchantId' value='" + merchantId + "'/>" +
                "<input type='hidden' name='accountId' value='" + accountId + "'/>" +
                "<input type='hidden' name='description' value='Compra TicketFlex'/>" +
                "<input type='hidden' name='referenceCode' value='" + referencia + "'/>" +
                "<input type='hidden' name='amount' value='" + montoStr + "'/>" +
                "<input type='hidden' name='currency' value='" + currency + "'/>" +
                "<input type='hidden' name='signature' value='" + firma + "'/>" +
                "<input type='hidden' name='buyerEmail' value='" + boleto.getUsuario().getCorreo() + "'/>" +
                "<input type='hidden' name='responseUrl' value='http://localhost:8080/confirmacion-payu'/>" +
                "<input type='submit' value='Pagar ahora'/>" +
                "</form></body></html>";

        response.setContentType("text/html");
        response.getWriter().write(formularioPayU);
    }

    @GetMapping("/confirmacion-payu")
    public ResponseEntity<String> confirmacionPayu(@RequestParam Map<String, String> allParams) {
        String referencia = allParams.get("referenceCode");
        String estadoTransaccion = allParams.get("transactionState");

        if (referencia == null) {
            return ResponseEntity.badRequest().body("Falta referencia");
        }

        String idTransaccion = referencia.replace("TX-", "");

        String objId;
        try {
            objId = new ObjectId(idTransaccion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("ID inválido: " + idTransaccion);
        }

        Optional<Transaccion> optTx = transaccionRepository.findById(objId);

        if (!optTx.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Transacción no encontrada con id: " + idTransaccion);
        }

        Transaccion tx = optTx.get();

        if ("4".equals(estadoTransaccion)) {
            tx.setEstadoPago("PAGADO");
        } else if ("6".equals(estadoTransaccion)) {
            tx.setEstadoPago("RECHAZADO");
        } else {
            tx.setEstadoPago("PENDIENTE");
        }
        transaccionRepository.save(tx);

        String mensaje = "Estado del pago: " + tx.getEstadoPago();

        String html = "<html><body>" +
                "<h1>Confirmación de pago</h1>" +
                "<p>Transacción: " + referencia + "</p>" +
                "<p>" + mensaje + "</p>" +
                "</body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

}
