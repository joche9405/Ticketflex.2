package com.tu_paquete.ticketflex.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RedirectController {

    @GetMapping("/reset-password")
    public String showResetPasswordPageWithoutToken() {
        // Para cuando acceden sin token (mostrará error en la página)
        return "reset-password";
    }

    @GetMapping("/reset-password/{token}")
    public String showResetPasswordPageWithToken(@PathVariable String token, Model model) {
        // Puedes pasar el token al modelo si lo necesitas en el HTML
        model.addAttribute("token", token);
        return "reset-password";
    }
}