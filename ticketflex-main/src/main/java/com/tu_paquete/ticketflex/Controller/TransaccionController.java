package com.tu_paquete.ticketflex.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Service.TransaccionService;
import com.tu_paquete.ticketflex.repository.mongo.TransaccionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {
    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @PostMapping("/crear")
    public ResponseEntity<Transaccion> crearTransaccion(@RequestBody Transaccion transaccion) {
        try {
            if (transaccion.getFechaPago() == null) {
                transaccion.setFechaPago(
                        java.util.Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            }
            if (transaccion.getEstadoPago() == null) {
                transaccion.setEstadoPago("pendiente");
            }

            System.out.println("Transacción a guardar: " + transaccion);
            Transaccion nuevaTransaccion = transaccionRepository.save(transaccion);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTransaccion);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/listar")
    public List<Transaccion> listarTransacciones() {
        return transaccionService.listarTransacciones();
    }

    @GetMapping("/{id}")
    public Transaccion obtenerTransaccionPorId(@PathVariable String id) {
        return transaccionService.obtenerTransaccionPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarTransaccion(@PathVariable String id) {
        transaccionService.eliminarTransaccion(id);
    }

    @GetMapping("/historial/{idUsuario}")
    public ResponseEntity<List<Transaccion>> obtenerHistorialDeCompras(@PathVariable String idUsuario) {
        List<Transaccion> historial = transaccionService.obtenerHistorialDeCompras(idUsuario);
        if (historial.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        return ResponseEntity.ok(historial);
    }

    @PostMapping("/confirmacion-payu")
    public ResponseEntity<String> confirmarPagoPayU(
            @RequestParam("reference_sale") String referenceCode,
            @RequestParam("state_pol") String estadoPago,
            @RequestParam("transaction_id") String transaccionIdPayU,
            @RequestParam("value") BigDecimal montopagado,
            @RequestParam("payment_method_type") String metodoPago,
            @RequestParam("currency") String moneda) {

        Optional<Transaccion> transaccionOpt = transaccionRepository.findByReferenceCode(referenceCode);
        if (transaccionOpt.isPresent()) {
            Transaccion transaccion = transaccionOpt.get();
            transaccion.setEstadoPago(traducirEstadoPayU(estadoPago));
            transaccion.setPayuTransactionId(transaccionIdPayU);
            transaccion.setMontoPagado(montopagado);
            transaccion.setMetodoPago(metodoPago);
            transaccion.setFechaPago(
                    java.util.Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            transaccionRepository.save(transaccion);
        }

        return ResponseEntity.ok("OK");
    }

    private String traducirEstadoPayU(String statePol) {
        return switch (statePol) {
            case "4" -> "APROBADO";
            case "5" -> "EXPIRADO";
            case "6" -> "RECHAZADO";
            case "7" -> "PENDIENTE";
            case "104" -> "ERROR";
            default -> "DESCONOCIDO";
        };
    }

    @PostMapping("/generar-pago")
    public ResponseEntity<String> generarFormularioPagoPayU(@RequestBody Transaccion transaccion) {
        try {
            String merchantId = "508029"; // Cambia esto por el tuyo
            String accountId = "512321"; // Cambia esto por el tuyo
            String apiKey = "4Vj8eK4rloUd272L48hsrarnUA"; // Cambia por tu llave real
            String referenceCode = "TX-" + System.currentTimeMillis(); // Puedes generar uno único

            String currency = "COP";
            BigDecimal monto = transaccion.getMontoPagado().setScale(2, RoundingMode.HALF_UP);

            String signatureBase = apiKey + "~" + merchantId + "~" + referenceCode + "~" + monto + "~" + currency;
            String signature = org.apache.commons.codec.digest.DigestUtils.md5Hex(signatureBase);

            String htmlForm = """
                    <html>
                        <body onload="document.forms['pagoForm'].submit()">
                            <form name="pagoForm" method="POST" action="https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/">
                                <input name="merchantId"    type="hidden"  value="%s"   >
                                <input name="accountId"     type="hidden"  value="%s" >
                                <input name="description"   type="hidden"  value="Pago boleta TicketFlex"  >
                                <input name="referenceCode" type="hidden"  value="%s" >
                                <input name="amount"        type="hidden"  value="%s" >
                                <input name="currency"      type="hidden"  value="%s" >
                                <input name="signature"     type="hidden"  value="%s"  >
                                <input name="buyerEmail"    type="hidden"  value="%s" >
                                <input name="responseUrl"   type="hidden"  value="https://tudominio.com/respuesta-pago" >
                                <input name="confirmationUrl" type="hidden" value="https://tudominio.com/api/transacciones/confirmacion-payu" >
                            </form>
                        </body>
                    </html>
                    """
                    .formatted(merchantId, accountId, referenceCode, monto, currency, signature,
                            transaccion.getEmailComprador());

            // Guarda la transacción con estado pendiente y el referenceCode
            transaccion.setReferenceCode(referenceCode);
            transaccion.setEstadoPago("pendiente");
            transaccion.setFechaPago(
                    java.util.Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));

            transaccionRepository.save(transaccion);

            return ResponseEntity.ok().body(htmlForm);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el formulario de pago");
        }
    }

}
