package com.tu_paquete.ticketflex.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

    @GetMapping("/politica-privacidad")
    public String politicaPrivacidad() {
        return "politica-privacidad";
    }

    @GetMapping("/terminos-servicio")
    public String terminosServicio() {
        return "terminos-servicio";
    }
}
