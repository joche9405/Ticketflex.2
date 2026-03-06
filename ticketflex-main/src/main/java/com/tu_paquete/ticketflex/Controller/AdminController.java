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
import org.springframework.format.annotation.DateTimeFormat;
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
        // 1. authentication.getName() ahora devuelve el ID del usuario (según tu
        // filtro)
        String userId = authentication.getName();

        // 2. Buscamos por ID en lugar de Email
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        // 3. El resto de tu lógica se mantiene igual
        List<Evento> eventos = eventoService.obtenerEventosPorCreador(usuario.getId());
        Long boletosVendidos = eventoService.contarBoletosVendidosPorCreador(usuario.getId());

        model.addAttribute("nombreUsuario", usuario.getNombre());
        model.addAttribute("apellidoUsuario", usuario.getApellido());
        model.addAttribute("emailUsuario", usuario.getEmail());
        model.addAttribute("eventos", eventos);
        model.addAttribute("totalBoletosVendidos", boletosVendidos);

        return "admin/dashboard";
    }

    @GetMapping("/eventos/crear")
    public String mostrarFormularioCrearEvento(Model model, Authentication authentication) {
        model.addAttribute("evento", new Evento()); // Agrega un objeto Evento vacío al modelo
        if (authentication != null) {
            System.out.println("Autoridades en el controlador: " + authentication.getAuthorities());
        }
        // Lista de categorías predefinidas
        List<String> categorias = Arrays.asList("Concierto", "Festival", "Teatro", "Conferencia", "Feria");
        model.addAttribute("categorias", categorias); // Enviar la lista a la vista

        return "admin/crear-evento"; // Renderiza la vista del formulario
    }

    // Método POST para procesar el formulario de creación
    @PostMapping("/eventos/crear")
    public ResponseEntity<?> crearEvento(
            @RequestParam String nombreEvento,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String lugar,
            @RequestParam String descripcion,
            @RequestParam BigDecimal precioBase,
            @RequestParam int disponibilidad,
            @RequestParam String categoria,
            @RequestParam(required = false) String artista,
            @RequestParam("imagen") MultipartFile imagenFile,
            Authentication authentication) {

        try {
            // 1. Validar autenticación
            if (authentication == null) {
                return ResponseEntity.status(401).body("{\"error\": \"No autenticado\"}");
            }

            // CORRECCIÓN: El getName() devuelve el ID del usuario, no el email
            String userId = authentication.getName();
            Usuario creador = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

            // 2. Crear objeto Evento
            Evento evento = new Evento();
            evento.setNombreEvento(nombreEvento);
            evento.setFecha(fecha);
            evento.setLugar(lugar);
            evento.setDescripcion(descripcion);
            evento.setPrecioBase(precioBase);
            evento.setDisponibilidad(disponibilidad);
            evento.setCategoria(categoria);
            evento.setArtista(artista);
            evento.setCreador(creador);

            // 3. Manejar la imagen en GridFS
            if (imagenFile != null && !imagenFile.isEmpty()) {
                ObjectId id = gridFsTemplate.store(
                        imagenFile.getInputStream(),
                        imagenFile.getOriginalFilename(),
                        imagenFile.getContentType());
                evento.setImagen(id.toString());
            } else {
                evento.setImagen("default.jpg");
            }

            // 4. Guardar: Usamos el email real obtenido del objeto 'creador'
            eventoService.crearEvento(evento, imagenFile, creador.getEmail());

            // 5. IMPORTANTE: Devolver JSON exitoso para el Fetch
            return ResponseEntity.ok().body("{\"message\": \"Evento creado correctamente\"}");

        } catch (Exception e) {
            e.printStackTrace();
            // Devolvemos 500 con el mensaje de error real para depurar en la consola del
            // navegador
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
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
    @ResponseBody
    public Map<String, Long> obtenerDatosEstadisticas(Authentication authentication) {
        // 1. Extraemos el ID del usuario (getName() devuelve el ID del token)
        String userId = authentication.getName();

        // 2. CORRECCIÓN: Buscar por ID, no por Email
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        // 3. Obtener eventos usando el ID real del usuario encontrado
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
        // 1. Obtener el ID del usuario desde la autenticación
        String userId = authentication.getName();

        // 2. Corregido: Buscar por ID en lugar de Email
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        System.out.println("ID del usuario autenticado: " + usuario.getId());

        // 1. Obtener solo los eventos del administrador actual
        List<EventoConEstadisticas> eventos = eventoRepository.findEventosConEstadisticas(usuario.getId());

        System.out.println("Número de eventos encontrados: " + eventos.size());

        // 2. Calcular estadísticas
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

        // 3. Agregar atributos al modelo
        model.addAttribute("totalBoletosVendidos", totalBoletosVendidos);
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("ocupacionPromedio", Math.round(ocupacionPromedio));
        model.addAttribute("eventos", eventos);

        return "admin/estadisticas";
    }

    @GetMapping("/estadisticas/eventos")
    @ResponseBody
    public List<Map<String, Object>> getEstadisticasEventos(Authentication authentication) {
        // 1. Obtener el ID del usuario
        String userId = authentication.getName();

        // 2. Corregido: Buscar por ID
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        List<EventoConEstadisticas> eventos = eventoRepository.findEventosConEstadisticas(usuario.getId());

        return eventos.stream().map(e -> {
            Map<String, Object> datos = new HashMap<>();
            datos.put("evento", e.getNombreEvento());
            datos.put("fecha", e.getFecha());
            datos.put("boletosVendidos", e.getBoletosVendidos());
            datos.put("capacidad", e.getCapacidad());
            datos.put("porcentajeOcupacion", e.getPorcentajeOcupacion());
            datos.put("ingresos", e.getIngresos());
            datos.put("categoria", e.getCategoria());
            datos.put("artista", e.getArtista());
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
        if (authentication == null)
            return "redirect:/login";

        String identifier = authentication.getName(); // Puede ser Email o ID

        // Intenta buscar por Email, si falla intenta por ID (ajusta según tu lógica)
        Usuario usuario = usuarioRepository.findByEmail(identifier)
                .orElseGet(() -> usuarioRepository.findById(identifier)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + identifier)));

        model.addAttribute("usuario", usuario);
        // VITAL para que el Navbar no rompa la página:
        model.addAttribute("nombreUsuario", usuario.getNombre());

        return "admin/perfil";
    }

    @PostMapping("/actualizarPerfil")
    public String actualizarPassword(@RequestParam("password") String nuevaPassword, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/admin/perfil?error=no_autenticado";
        }

        String identifier = authentication.getName();

        // Busca por Email o por ID según lo que guardes en el JWT
        Usuario usuario = usuarioRepository.findByEmail(identifier)
                .orElseGet(() -> usuarioRepository.findById(identifier)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado")));

        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            // Asegúrate de que este método realmente devuelva el hash (BCrypt)
            String passwordEncriptada = usuarioService.encriptarPassword(nuevaPassword);
            usuario.setPassword(passwordEncriptada);

            // ¡IMPORTANTE! Asegúrate de que el repositorio guarde los cambios
            usuarioRepository.save(usuario);
        }

        return "redirect:/admin/perfil?success";
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