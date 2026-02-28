package com.tu_paquete.ticketflex.Controller;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.BoletoService;
import com.tu_paquete.ticketflex.Service.TransaccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private BoletoService boletoService;

    @Autowired
    private TransaccionService transaccionService;

    @GetMapping("/boletos")
    public ResponseEntity<List<Boleto>> getUserBoletos(@AuthenticationPrincipal Usuario usuario) {
        List<Boleto> boletos = boletoService.findByUsuarioId(usuario.getId());
        return ResponseEntity.ok(boletos);
    }

    @GetMapping("/transacciones")
    public ResponseEntity<Page<Transaccion>> getUserTransacciones(
            @AuthenticationPrincipal Usuario usuario,
            Pageable pageable) {
        Page<Transaccion> transacciones = transaccionService.findByUsuarioId(usuario.getId(), pageable);
        return ResponseEntity.ok(transacciones);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Transaccion>> filterPurchases(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) String filter) {
        List<Transaccion> transacciones = transaccionService.filterByDateRange(
                usuario.getId(),
                filter != null ? filter : "all");
        return ResponseEntity.ok(transacciones);
    }
}
