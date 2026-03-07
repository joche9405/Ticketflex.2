package com.tu_paquete.ticketflex.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuperAdminViewController {

    @GetMapping("/superadmin/login") // Esta es la ruta para el navegador
    public String mostrarLoginSuperAdmin() {
        return "login-superadmin"; // Busca login-superadmin.html en /templates
    }
}