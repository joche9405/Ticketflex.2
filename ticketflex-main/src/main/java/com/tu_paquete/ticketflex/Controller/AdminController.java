package com.tu_paquete.ticketflex.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.FuncionEvento;
import com.tu_paquete.ticketflex.Model.PerfilUsuario;
import com.tu_paquete.ticketflex.Model.PrediccionResultado;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.EventoService;
import com.tu_paquete.ticketflex.Service.PrediccionEventoService;
import com.tu_paquete.ticketflex.Service.UsuarioService;
import com.tu_paquete.ticketflex.Service.dto.NuevaPersonaRequest;
import com.tu_paquete.ticketflex.Service.dto.PrediccionMasivaResultado;
import com.tu_paquete.ticketflex.dto.EventoConEstadisticas;
import com.tu_paquete.ticketflex.repository.jpa.FuncionEventoRepository;
import com.tu_paquete.ticketflex.repository.jpa.PerfilUsuarioRepository;
import com.tu_paquete.ticketflex.repository.mongo.EventoRepository;
import com.tu_paquete.ticketflex.repository.mongo.UsuarioRepository;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

@Controller

@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private PrediccionEventoService prediccionEventoService;

    @Autowired
    private FuncionEventoRepository funcionEventoRepository;

    @Autowired
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @Autowired
    private PrediccionEventoService prediccionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Obtener el usuario autenticado
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener solo los eventos del usuario autenticado
        List<Evento> eventos = eventoService.obtenerEventosPorCreador(usuario.getId());
        Long boletosVendidos = eventoService.contarBoletosVendidosPorCreador(usuario.getId());

        // Agregar atributos al modelo
        model.addAttribute("nombreUsuario", usuario.getNombre()); // Asumiendo que tienes getNombre()
        model.addAttribute("apellidoUsuario", usuario.getApellido()); // Si necesitas el apellido
        model.addAttribute("emailUsuario", usuario.getEmail()); // Si necesitas el email
        model.addAttribute("eventos", eventos);
        model.addAttribute("totalBoletosVendidos", boletosVendidos); // Envía el total filtrado

        return "admin/dashboard";
    }

    @GetMapping("/eventos/crear")
    public String mostrarFormularioCrearEvento(Model model) {
        model.addAttribute("evento", new Evento()); // Agrega un objeto Evento vacío al modelo

        // Lista de categorías predefinidas
        List<String> categorias = Arrays.asList("Concierto", "Festival", "Teatro", "Conferencia", "Feria");
        model.addAttribute("categorias", categorias); // Enviar la lista a la vista

        return "admin/crear-evento"; // Renderiza la vista del formulario
    }

    // Método POST para procesar el formulario de creación
    @PostMapping("/eventos/crear")
    public String crearEvento(
            @RequestParam String nombreEvento,
            @RequestParam LocalDate fecha,
            @RequestParam String lugar,
            @RequestParam String descripcion,
            @RequestParam BigDecimal precioBase,
            @RequestParam int disponibilidad,
            @RequestParam String categoria,
            @RequestParam(required = false) String artista,
            @RequestParam("imagen") MultipartFile imagenFile,
            Authentication authentication) throws IOException {

        // Crear objeto Evento manualmente
        Evento evento = new Evento();
        evento.setNombreEvento(nombreEvento);
        evento.setFecha(fecha);
        evento.setLugar(lugar);
        evento.setDescripcion(descripcion);
        evento.setPrecioBase(precioBase);
        evento.setDisponibilidad(disponibilidad);
        evento.setCategoria(categoria);
        evento.setArtista(artista);

        // Obtener usuario autenticado
        String email = authentication.getName();
        Usuario creador = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        evento.setCreador(creador);

        // Manejar la imagen
        if (imagenFile != null && !imagenFile.isEmpty()) {
            ObjectId id = gridFsTemplate.store(
                    imagenFile.getInputStream(),
                    imagenFile.getOriginalFilename(),
                    imagenFile.getContentType());
            evento.setImagen(id.toString());
        } else {
            evento.setImagen("default.jpg");
        }

        // Guardar el evento
        eventoService.crearEvento(evento, imagenFile, email);

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/eventos/listar")
    public String listarEventos(Model model) {
        // Aquí puedes obtener la lista de eventos desde el servicio y pasarla al modelo
        return "admin/listar-eventos"; // Renderiza la lista de eventos
    }

    // Método GET para mostrar el formulario de edición
    @GetMapping("/eventos/editar/{id}")
    public String mostrarFormularioEditarEvento(@PathVariable String id, Model model) {
        Evento evento = eventoService.obtenerEventoPorId(id);
        if (evento == null) {
            return "redirect:/admin/eventos/listar";
        }

        // Asegurar que siempre haya una imagen asociada
        if (evento.getImagen() == null || evento.getImagen().isEmpty()) {
            evento.setImagen("default.jpg");
        }

        // Agregar URL completa de la imagen
        model.addAttribute("imagenUrl", "/api/imagen/" + evento.getImagen());
        model.addAttribute("evento", evento);

        // Agregar categorías si las necesitas
        List<String> categorias = Arrays.asList("Concierto", "Festival", "Teatro", "Conferencia", "Feria");
        model.addAttribute("categorias", categorias);

        return "admin/editar-evento";
    }

    // Método POST para procesar el formulario de edición
    @PostMapping("/eventos/editar/{id}")
    public String actualizarEvento(
            @PathVariable String id,
            @ModelAttribute("evento") Evento eventoActualizado,
            @RequestParam(value = "nuevaImagen", required = false) MultipartFile nuevaImagen,
            Authentication authentication,
            RedirectAttributes redirectAttributes) throws IOException {

        // 1. Obtener el evento existente
        Evento eventoExistente = eventoService.obtenerEventoPorId(id);
        if (eventoExistente == null) {
            redirectAttributes.addFlashAttribute("error", "El evento no existe");
            return "redirect:/admin/dashboard";
        }

        // 2. Actualizar campos editables
        eventoExistente.setNombreEvento(eventoActualizado.getNombreEvento());
        eventoExistente.setFecha(eventoActualizado.getFecha());
        eventoExistente.setLugar(eventoActualizado.getLugar());
        eventoExistente.setDescripcion(eventoActualizado.getDescripcion());
        eventoExistente.setPrecioBase(eventoActualizado.getPrecioBase());
        eventoExistente.setDisponibilidad(eventoActualizado.getDisponibilidad());
        eventoExistente.setCategoria(eventoActualizado.getCategoria());
        eventoExistente.setArtista(eventoActualizado.getArtista());

        // 3. Manejo de la imagen (solo si se sube una nueva)
        if (nuevaImagen != null && !nuevaImagen.isEmpty()) {
            // Eliminar imagen anterior si existe y no es la por defecto
            if (eventoExistente.getImagen() != null && !eventoExistente.getImagen().equals("default.jpg")) {
                gridFsTemplate.delete(Query.query(Criteria.where("_id").is(new ObjectId(eventoExistente.getImagen()))));
            }

            // Subir nueva imagen a GridFS
            ObjectId idImagen = gridFsTemplate.store(
                    nuevaImagen.getInputStream(),
                    nuevaImagen.getOriginalFilename(),
                    nuevaImagen.getContentType());
            eventoExistente.setImagen(idImagen.toString());
        }

        // 4. Guardar cambios
        eventoService.actualizarEvento(eventoExistente);

        redirectAttributes.addFlashAttribute("success", "Evento actualizado correctamente");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/estadisticas-data")
    @ResponseBody // Indica que el valor de retorno debe ser serializado directamente al cuerpo de
                  // la respuesta (ej. JSON)
    public Map<String, Long> obtenerDatosEstadisticas(Authentication authentication) {
        // ... (la misma lógica para obtener las estadísticas que antes) ...
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<EventoConEstadisticas> eventos = eventoRepository.findEventosConEstadisticas(usuario.getId());
        Long totalBoletosVendidos = eventos.stream()
                .mapToLong(EventoConEstadisticas::getBoletosVendidos)
                .sum();

        Map<String, Long> estadisticas = new HashMap<>();
        estadisticas.put("totalBoletosVendidos", totalBoletosVendidos);
        return estadisticas;
    }

    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(Model model, Authentication authentication) {
        // Obtener el usuario/admin logueado
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("ID del usuario: " + usuario.getId());

        // 1. Obtener solo los eventos del administrador actual
        // Cambio: El ID en MongoDB es String en lugar de Integer
        List<EventoConEstadisticas> eventos = eventoRepository.findEventosConEstadisticas(usuario.getId());

        System.out.println("Número de eventos encontrados: " + eventos.size());
        eventos.forEach(e -> System.out.println(
                "Evento: " + e.getNombreEvento() +
                        " | Boletos: " + e.getBoletosVendidos() +
                        " | Capacidad: " + e.getCapacidad() +
                        " | Ingresos: " + e.getIngresos()));

        // 2. Calcular estadísticas SOLO para los eventos de este administrador
        Long totalBoletosVendidos = eventos.stream()
                .mapToLong(EventoConEstadisticas::getBoletosVendidos)
                .sum();

        BigDecimal ingresosTotales = eventos.stream()
                .map(EventoConEstadisticas::getIngresos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double ocupacionPromedio = eventos.stream()
                .mapToInt(EventoConEstadisticas::getPorcentajeOcupacion)
                .average()
                .orElse(0.0);

        model.addAttribute("totalBoletosVendidos", totalBoletosVendidos);
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("ocupacionPromedio", Math.round(ocupacionPromedio));
        model.addAttribute("eventos", eventos);

        return "admin/estadisticas";
    }

    @GetMapping("/estadisticas/eventos")
    @ResponseBody
    public List<Map<String, Object>> getEstadisticasEventos(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<EventoConEstadisticas> eventos = eventoRepository.findEventosConEstadisticas(usuario.getId());

        return eventos.stream().map(e -> {
            Map<String, Object> datos = new HashMap<>();
            datos.put("evento", e.getNombreEvento());
            datos.put("fecha", e.getFecha());
            datos.put("boletosVendidos", e.getBoletosVendidos());
            datos.put("capacidad", e.getCapacidad());
            datos.put("porcentajeOcupacion", e.getPorcentajeOcupacion());
            datos.put("ingresos", e.getIngresos());
            datos.put("categoria", e.getCategoria()); // 👈 NUEVO
            datos.put("artista", e.getArtista()); // 👈 NUEVO
            return datos;
        }).toList();
    }

    // Método GET para eliminar el evento
    @GetMapping("/eventos/eliminar/{id}")
    public String eliminarEvento(@PathVariable String id) {
        // Obtener el evento por ID
        Evento evento = eventoService.obtenerEventoPorId(id);

        if (evento != null) {
            // Eliminar el evento
            eventoService.eliminarEvento(id);
        }

        // Redirigir a la lista de eventos después de eliminar
        return "redirect:/admin/eventos/listar";
    }

    @GetMapping("/predecir-individual-form")
    public String mostrarFormularioPrediccionIndividual(Model model) {
        List<PerfilUsuario> usuarios = perfilUsuarioRepository.findAll();
        List<FuncionEvento> eventos = funcionEventoRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("eventos", eventos);
        model.addAttribute("resultado", null); // Para mostrar el resultado individual
        return "admin/predecir-evento-form";
    }

    @PostMapping("/predecir")
    public String predecirAsistenciaDesdeFormulario(
            @RequestParam("idUsuario") Long idUsuario,
            @RequestParam("idEvento") Long idEvento,
            Model model) {
        try {
            com.tu_paquete.ticketflex.Service.dto.ResultadoPrediccion resultado = prediccionEventoService
                    .predecirAsistenciaEvento(idUsuario, idEvento);
            model.addAttribute("resultado", resultado);
            List<PerfilUsuario> usuarios = perfilUsuarioRepository.findAll();
            List<FuncionEvento> eventos = funcionEventoRepository.findAll();
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("eventos", eventos);
            return "admin/predecir-evento-form";
        } catch (Exception e) {
            model.addAttribute("error", "Error al realizar la predicción: " + e.getMessage());
            List<PerfilUsuario> usuarios = perfilUsuarioRepository.findAll();
            List<FuncionEvento> eventos = funcionEventoRepository.findAll();
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("eventos", eventos);
            model.addAttribute("resultado", null);
            return "admin/predecir-evento-form";
        }
    }

    @GetMapping("/predecir-evento")
    public String mostrarFormularioSeleccionEvento(Model model) {
        List<FuncionEvento> eventos = funcionEventoRepository.findAll();
        model.addAttribute("eventos", eventos);
        model.addAttribute("resultados", null); // Para la tabla de resultados masivos
        return "admin/seleccionar-evento-form";
    }

    @PostMapping("/predecir-asistencia-masiva")
    public String predecirAsistenciaParaEvento(@RequestParam("idEvento") Long idEvento, Model model) {
        try {
            List<PrediccionMasivaResultado> resultados = prediccionEventoService
                    .predecirAsistenciaEventoMasivo(idEvento);
            FuncionEvento evento = funcionEventoRepository.findById(idEvento).orElse(null);
            model.addAttribute("evento", evento);
            model.addAttribute("resultados", resultados);
            return "admin/resultados-prediccion"; // <--- Especifica la ruta admin/
        } catch (Exception e) {
            model.addAttribute("error", "Error al realizar la predicción masiva: " + e.getMessage());
            return "admin/error"; // <--- Especifica la ruta admin/ también para la página de error
        }
    }

    @PostMapping("/predecir-persona-nueva")
    public ResponseEntity<PrediccionResultado> predecirNuevaPersona(
            @RequestBody NuevaPersonaRequest nuevaPersonaRequest) {
        try {
            PrediccionResultado resultado = prediccionService.predecirAsistenciaNuevaPersona(nuevaPersonaRequest);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PrediccionResultado("Error", "0%"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/exportar-excel")
    public void exportarResultadosExcel(@RequestParam("idEvento") Long idEvento, HttpServletResponse response)
            throws Exception {
        FuncionEvento evento = prediccionEventoService.obtenerEventoPorId(idEvento); // Asegúrate de tener este método
                                                                                     // en tu servicio
        List<PrediccionMasivaResultado> resultados = prediccionEventoService.predecirAsistenciaEventoMasivo(idEvento);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Predicciones de Asistencia");

        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID Usuario");
        headerRow.createCell(1).setCellValue("Edad");
        headerRow.createCell(2).setCellValue("Género");
        headerRow.createCell(3).setCellValue("Predicción");
        headerRow.createCell(4).setCellValue("Confianza");
        headerRow.createCell(5).setCellValue("Historial Compras"); // Nuevo encabezado
        headerRow.createCell(6).setCellValue("Interés Principal"); // Nuevo encabezado
        headerRow.createCell(7).setCellValue("Evento"); // Nombre del evento

        // Llenar los datos
        int rowNum = 1;
        for (PrediccionMasivaResultado resultado : resultados) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(resultado.getIdUsuario());
            row.createCell(1).setCellValue(resultado.getEdadUsuario());
            row.createCell(2).setCellValue(resultado.getGeneroUsuario());
            row.createCell(3).setCellValue(resultado.getPrediccion());
            row.createCell(4).setCellValue(resultado.getConfianza());
            row.createCell(5).setCellValue(resultado.getHistorialCompras()); // Agregar dato
            row.createCell(6).setCellValue(resultado.getInteresCategoria());
            row.createCell(7).setCellValue(evento.getNombreFuncion());
        }

        // Ajustar el ancho de las columnas
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        // Configurar la respuesta HTTP
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment; filename=predicciones_evento_" + evento.getIdFuncion() + ".xlsx");

        // Escribir el libro de Excel en la respuesta
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // Asegúrate de tener un método para obtener el evento por ID en tu servicio
    @GetMapping("/evento/{id}")
    public String obtenerEvento(@PathVariable Long id, Model model) {
        FuncionEvento evento = funcionEventoRepository.findById(id).orElse(null);
        model.addAttribute("evento", evento);
        return "admin/evento-detalle"; // O la vista que necesites
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("usuario", usuario);
        return "admin/perfil"; // Ruta del archivo HTML en templates/admin/perfil.html
    }

    @PostMapping("/actualizarPerfil")
    public String actualizarContraseña(@RequestParam("password") String nuevaPassword, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/admin/perfil?error=no_autenticado";
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (nuevaPassword != null && !nuevaPassword.isEmpty()) {
            String passwordEncriptada = usuarioService.encriptarPassword(nuevaPassword);
            usuario.setPassword(passwordEncriptada);
            usuarioService.updateUsuario(usuario);
        }

        return "redirect:/admin/perfil?success"; // en vez de ?success=contrasena_actualizada
    }

    // --- Olvidé mi contraseña (ADMIN) ---

    @GetMapping("/forgot-password")
    public String mostrarFormularioOlvidePasswordAdmin() {
        return "admin/forgot-password-admin"; // Vista con formulario de email
    }

    @PostMapping("/reset-password-request")
    public String procesarSolicitudResetPasswordAdmin(@RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.generarTokenDeReseteo(email, true); // <-- nombre correcto
            redirectAttributes.addFlashAttribute("mensaje",
                    "Si el correo existe, se enviará un enlace para restablecer la contraseña.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la solicitud.");
        }
        return "redirect:/admin/forgot-password";
    }

    @GetMapping("/reset-password/{token}")
    public String mostrarFormularioResetPasswordAdmin(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        return "admin/reset-password-admin"; // Vista con form de nueva contraseña
    }

    @PostMapping("/reset-password")
    public String procesarResetPasswordAdmin(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String nuevaPassword,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.resetearPassword(token, nuevaPassword);
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña actualizada con éxito.");
            return "redirect:/login"; // o la ruta del login de admin
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/reset-password/" + token;
        }
    }

}