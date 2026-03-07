package com.tu_paquete.ticketflex.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Renderiza la vista de inicio de sesión
    }

    @GetMapping("/login-superadmin")
    public String mostrarLoginSuperAdmin() {
        return "login-superadmin"; // Esto busca el archivo login-superadmin.html en templates
    }
}