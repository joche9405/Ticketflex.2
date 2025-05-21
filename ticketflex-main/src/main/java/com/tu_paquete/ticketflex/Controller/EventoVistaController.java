package com.tu_paquete.ticketflex.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Service.EventoService;

import org.springframework.ui.Model;

@RequestMapping("/eventos")
@Controller
public class EventoVistaController {

    @Autowired
    private EventoService eventoService;

    @GetMapping("/eventoseleccionado/{id}")
    public String mostrarEvento(@PathVariable String id, Model model) {
        Evento evento = eventoService.obtenerEventoPorId(id);
        model.addAttribute("evento", evento);
        return "eventoseleccionado"; // Thymeleaf buscará eventoseleccionado.html en /templates
    }
}

