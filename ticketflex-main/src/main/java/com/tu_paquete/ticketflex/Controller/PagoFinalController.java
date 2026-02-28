package com.tu_paquete.ticketflex.Controller;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.repository.mongo.BoletoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class PagoFinalController {

    @Autowired
    private BoletoRepository boletoRepository;

    @GetMapping("/completar-pago")
    public ResponseEntity<String> completarPago(@RequestParam String id) {
        System.out.println("Intentando buscar boleto con id: " + id);
        Optional<Boleto> boletoOptional = boletoRepository.findById(id);
        if (!boletoOptional.isPresent()) {
            System.out.println("No se encontró boleto con id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Boleto no encontrado");
        }
        Boleto boleto = boletoOptional.get();
        System.out.println("Boleto encontrado: " + boleto);
        boletoRepository.save(boleto);
        return ResponseEntity.ok("Pago completado para boleto id: " + id);
    }
}
