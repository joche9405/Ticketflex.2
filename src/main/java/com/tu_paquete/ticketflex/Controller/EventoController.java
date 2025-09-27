package com.tu_paquete.ticketflex.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.BoletoService;
import com.tu_paquete.ticketflex.Service.EventoService;
import com.tu_paquete.ticketflex.Service.PayUService;
import com.tu_paquete.ticketflex.dto.PagoRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    @Autowired
    private EventoService eventoService;
    @Autowired
    private BoletoService boletoService;
    @Autowired
    private PayUService payUService;

    @GetMapping("/listar")
    public ResponseEntity<?> listarEventos() {
        try {
            List<Evento> eventos = eventoService.listarEventos();

            // Convertir a un formato que Jackson pueda serializar fácilmente
            List<Map<String, Object>> response = eventos.stream().map(evento -> {
                Map<String, Object> map = new HashMap<>();
                try {
                    map.put("id", evento.getId());
                    map.put("nombreEvento", evento.getNombreEvento());
                    map.put("artista", evento.getArtista());
                    map.put("fecha", evento.getFecha() != null ? evento.getFecha().toString() : null);
                    map.put("lugar", evento.getLugar());
                    map.put("descripcion", evento.getDescripcion());
                    map.put("precioBase", evento.getPrecioBase() != null ? evento.getPrecioBase().toString() : null);
                    map.put("disponibilidad", evento.getDisponibilidad());
                    // Usar el ID de la imagen en lugar de la imagen binaria
                    String imagenId = evento.getImagen();
                    map.put("imagen", imagenId != null ? "/api/imagen/" + imagenId : "/api/imagen/default.jpg");
                    map.put("categoria", evento.getCategoria());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al cargar los eventos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }

    @GetMapping("/{id}")
    public Evento obtenerEventoPorId(@PathVariable String id) {
        return eventoService.obtenerEventoPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarEvento(@PathVariable String id) {
        eventoService.eliminarEvento(id);
    }

    // Actualizar un evento
    @PutMapping("/{id}")
    public ResponseEntity<Evento> actualizarEvento(
            @PathVariable String id,
            @ModelAttribute Evento eventoActualizado, // Para datos formulario
            @RequestParam("imagenFile") MultipartFile imagenFile) throws IOException {

        Evento eventoExistente = eventoService.obtenerEventoPorId(id);
        if (eventoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // Actualiza campos normales
        eventoExistente.setNombreEvento(eventoActualizado.getNombreEvento());
        eventoExistente.setFecha(eventoActualizado.getFecha());
        eventoExistente.setLugar(eventoActualizado.getLugar());
        eventoExistente.setPrecioBase(eventoActualizado.getPrecioBase());

        // Manejo de imagen
        if (imagenFile != null && !imagenFile.isEmpty()) {
            String nombreArchivo = UUID.randomUUID() + "_" + imagenFile.getOriginalFilename();
            Path rutaArchivo = Paths.get("uploads").resolve(nombreArchivo);
            Files.copy(imagenFile.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            eventoExistente.setImagen(nombreArchivo);
        } else if (eventoActualizado.getImagen() == null || eventoActualizado.getImagen().trim().isEmpty()) {
            eventoExistente.setImagen("default.jpg");
        }

        Evento actualizado = eventoService.actualizarEvento(eventoExistente);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/{id_evento}/comprar")
    public ResponseEntity<Map<String, Object>> mostrarPaginaCompra(
            @PathVariable String id_evento,
            Authentication authentication) {

        try {
            // Obtener evento
            Evento evento = eventoService.obtenerEventoPorId(id_evento);
            if (evento == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Evento no encontrado"));
            }

            // Obtener usuario autenticado
            Usuario usuario = (Usuario) authentication.getPrincipal();

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("evento", evento);
            response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "email", usuario.getEmail()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id_evento}/comprar")
    public ResponseEntity<String> comprarBoleto(
            @PathVariable String id_evento,
            @RequestBody Map<String, Object> compraRequest,
            Authentication authentication) {

        try {
            // Validar datos
            Integer cantidad = (Integer) compraRequest.get("cantidad");
            String tipoBoleto = (String) compraRequest.get("tipoBoleto");

            if (cantidad == null || cantidad <= 0 || cantidad > 5) {
                return ResponseEntity.badRequest()
                        .body("<h3>Cantidad inválida (1-5)</h3>");
            }

            if (tipoBoleto == null || (!tipoBoleto.equals("BASE") && !tipoBoleto.equals("VIP"))) {
                return ResponseEntity.badRequest()
                        .body("<h3>Tipo de boleto inválido</h3>");
            }

            // Usuario autenticado
            Usuario usuario = (Usuario) authentication.getPrincipal();

            // Lógica de compra y creación del boleto
            Boleto boleto = boletoService.comprarBoleto(
                    id_evento,
                    usuario.getId(),
                    cantidad);

            // Llamar a PayUService para generar el HTML de redirección
            String formularioHtml = payUService.generarFormularioRedireccion(boleto);

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html")
                    .body(formularioHtml);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<h3>Error: " + e.getMessage() + "</h3>");
        }
    }

    // Endpoint para filtrar los eventos
    @PostMapping("/filtrar")
    public List<Evento> filtrarEventos(@RequestBody Map<String, Object> filtros) {
        // Obtener los parámetros de los filtros
        String lugar = (String) filtros.get("lugar");
        String fechaStr = (String) filtros.get("fecha");
        String categoria = (String) filtros.get("categoria");
        String artista = (String) filtros.get("artista");

        // Convertir la fecha de String a LocalDate
        LocalDate fecha = null;
        if (fechaStr != null && !fechaStr.isEmpty()) {
            try {
                fecha = LocalDate.parse(fechaStr);
            } catch (DateTimeParseException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha inválida", e);
            }
        }

        // Llamar al servicio de filtrado
        return eventoService.filtrarEventos(lugar, fecha, categoria, artista);
    }

    @PostMapping("/pagar")
    public ResponseEntity<String> procesarPago(@RequestBody PagoRequest pagoRequest) {

        return ResponseEntity.ok("Pago procesado con éxito");
    }

    // NUEVOS ENDPOINT

    @GetMapping("/mis-eventos")
    public ResponseEntity<List<Evento>> getMisEventos(Authentication authentication) {
        // Verificar autenticación
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Obtener usuario
        Usuario usuarioActual = (Usuario) authentication.getPrincipal();
        System.out.println("Usuario autenticado ID: " + usuarioActual.getId());

        // Obtener eventos
        List<Evento> eventos = eventoService.obtenerEventosPorCreador(usuarioActual.getId());
        System.out.println("Eventos encontrados: " + eventos.size());

        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/mis-eventos/paginados")
    public ResponseEntity<Page<Evento>> getMisEventosPaginados(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Usuario usuarioActual = (Usuario) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<Evento> eventos = eventoService.obtenerEventosPorCreadorId(usuarioActual.getId(), pageable);

        return ResponseEntity.ok(eventos);
    }

}
