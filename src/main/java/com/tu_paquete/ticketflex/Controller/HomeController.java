package com.tu_paquete.ticketflex.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/") // Cambiado a raíz
    public String index() {
        return "index"; // Asegúrate de que exista src/main/resources/templates/index.html
    }
}

