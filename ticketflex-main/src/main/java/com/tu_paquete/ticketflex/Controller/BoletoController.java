package com.tu_paquete.ticketflex.Controller;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.Model.Boleto;

import com.tu_paquete.ticketflex.Service.BoletoService;
import com.tu_paquete.ticketflex.Service.PayUService;

@RestController
@RequestMapping("/api/boletas")
public class BoletoController {
	
    @Autowired
    private BoletoService boletoService;
    @Autowired
    private PayUService payUService;
    @PostMapping("/comprar")
    public ResponseEntity<?> comprarBoleto(
            @RequestParam String idEvento,
            @RequestParam String idUsuario,
            @RequestParam Integer cantidad) {
        try {
            Boleto boleto = boletoService.comprarBoleto(idEvento, idUsuario, cantidad);
            return ResponseEntity.ok(boleto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    
    @GetMapping("/pagar/{boletoId}")
    public ResponseEntity<String> pagarBoleto(@PathVariable String boletoId) {
    	Boleto boleto = boletoService.obtenerPorId(boletoId);  // Cambia si usas otro método
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

