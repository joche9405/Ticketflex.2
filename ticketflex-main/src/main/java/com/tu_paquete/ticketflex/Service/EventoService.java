package com.tu_paquete.ticketflex.Service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.repository.mongo.BoletoRepository;
import com.tu_paquete.ticketflex.repository.mongo.EventoRepository;
import com.tu_paquete.ticketflex.repository.mongo.UsuarioRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class EventoService {

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFsOperations gridFsOperations;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private BoletoRepository boletoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Evento crearEvento(Evento evento, MultipartFile imagen, String emailCreador) throws IOException {
        // Validar que el evento tenga un creador
        if (evento.getCreador() == null) {
            throw new IllegalArgumentException("El evento debe tener un usuario creador");
        }

        // Subir imagen a GridFS si se envió una
        if (imagen != null && !imagen.isEmpty()) {
            ObjectId id = gridFsTemplate.store(imagen.getInputStream(), imagen.getOriginalFilename(),
                    imagen.getContentType());
            evento.setImagen(id.toString()); // Guarda el ID del archivo en Mongo
        } else {
            // Si no se envía imagen, usar una por defecto (ya subida a GridFS)
            evento.setImagen("default.jpg");
        }

        return eventoRepository.save(evento);
    }

    public List<Evento> listarEventos() {
        try {
            List<Evento> eventos = eventoRepository.findAll();
            // Asegurarse de que cada evento tenga un ID de imagen válido
            for (Evento evento : eventos) {
                if (evento.getImagen() == null || evento.getImagen().isEmpty()) {
                    evento.setImagen("default.jpg");
                } else {
                    try {
                        // Verificar si la imagen existe en GridFS
                        Query query = Query.query(Criteria.where("_id").is(new ObjectId(evento.getImagen())));
                        if (gridFsTemplate.findOne(query) == null) {
                            evento.setImagen("default.jpg");
                        }
                    } catch (IllegalArgumentException e) {
                        // Si el ID no es un ObjectId válido
                        evento.setImagen("default.jpg");
                    }
                }
            }
            return eventos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar eventos: " + e.getMessage());
        }
    }

    public Evento obtenerEventoPorId(String id) {
        return eventoRepository.findById(id).orElse(null); // Cambiado de Integer a String para MongoDB
    }

    public void eliminarEvento(String id) {
        eventoRepository.deleteById(id); // Cambiado de Integer a String para MongoDB
    }

    public boolean comprarBoleto(String eventoId, Integer cantidad, String usuarioId) {
        // Obtener el evento
        Evento evento = obtenerEventoPorId(eventoId);
        if (evento == null) {
            return false; // Evento no encontrado
        }

        // Verificar si hay suficientes boletos disponibles
        if (evento.getDisponibilidad() != null && evento.getDisponibilidad() >= cantidad) {
            // Restar la cantidad comprada de la disponibilidad
            evento.setDisponibilidad(evento.getDisponibilidad() - cantidad);
            eventoRepository.save(evento); // Guardar el evento actualizado

            // Obtener el usuario
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            if (usuario == null) {
                return false; // Usuario no encontrado
            }

            // 3) Crear embeds
            Boleto.EventoEmbed evEmb = new Boleto.EventoEmbed();
            evEmb.setId(evento.getId().toString());
            evEmb.setNombre(evento.getNombreEvento());
            evEmb.setFecha(evento.getFecha()); // LocalDate directo ✅

            Boleto.UsuarioEmbed uEmb = new Boleto.UsuarioEmbed();
            uEmb.setId(usuario.getId());
            uEmb.setNombre(usuario.getNombre());
            uEmb.setCorreo(usuario.getEmail());

            // 4) Crear y guardar Boleto
            Boleto boleto = new Boleto();
            boleto.setEvento(evEmb);
            boleto.setUsuario(uEmb);
            boleto.setCantidad(cantidad);
            boleto.setPrecio(evento.getPrecioBase());
            boleto.setPrecioTotal(evento.getPrecioBase().multiply(BigDecimal.valueOf(cantidad)));
            boleto.setFechaCompra(

                    LocalDate.now());

            Boleto boletoGuardado = boletoRepository.save(boleto);

            // 5) Registrar Transacción
            Transaccion trx = new Transaccion();
            trx.setBoletoId(boletoGuardado.getId());
            trx.setUsuarioId(usuario.getId());
            trx.setEventoId(evento.getId().toString());
            trx.setCantidadBoletos(cantidad);
            trx.setTotal(boletoGuardado.getPrecioTotal());
            trx.setFechaPago(new Date());
            trx.setEstadoPago("COMPLETADO");

            transaccionService.crearTransaccion(trx);

            return true;
        } else {
            return false; // No hay suficientes boletos disponibles
        }
    }

    public Evento actualizarEvento(Evento eventoActualizado) {
        // Guarda el evento actualizado en la base de datos
        return eventoRepository.save(eventoActualizado); // Este es el método que persiste el evento actualizado
    }

    // Filtros de eventos
    public List<Evento> filtrarEventos(String lugar, LocalDate fecha, String categoria, String artista) {
        // Si no se especifica ningún filtro, devolver todos los eventos
        if (lugar == null && fecha == null && categoria == null && artista == null) {
            return eventoRepository.findAll();
        }

        // Si todos los filtros son no nulos, aplicarlos de manera combinada
        if (lugar != null && fecha != null && categoria != null && artista != null) {
            return eventoRepository.findByLugarAndFechaAndCategoriaAndArtista(lugar, fecha, categoria, artista);
        }

        // Filtrar por lugar
        if (lugar != null && !lugar.isEmpty()) {
            return eventoRepository.findByLugar(lugar);
        }

        // Filtrar por fecha
        if (fecha != null) {
            return eventoRepository.findByFecha(fecha);
        }

        // Filtrar por categoría
        if (categoria != null && !categoria.isEmpty()) {
            return eventoRepository.findByCategoria(categoria);
        }

        // Filtrar por artista
        if (artista != null && !artista.isEmpty()) {
            return eventoRepository.findByArtista(artista);
        }

        // Si no se puede hacer filtrado por combinación, retornar todos
        return eventoRepository.findAll();
    }

    // METODOS SUBIR UNA IMAGEN

    public String guardarImagen(MultipartFile file) throws IOException {
        // Sube el archivo y retorna el ObjectId como String
        ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        return id.toString();
    }

    public GridFSFile obtenerArchivo(String imagenId) {
        return gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(imagenId)));
    }

    public InputStreamResource obtenerImagenComoRecurso(String imagenId) throws IOException {
        GridFSFile archivo = obtenerArchivo(imagenId);
        GridFsResource recurso = gridFsOperations.getResource(archivo);
        return new InputStreamResource(recurso.getInputStream());
    }

    // Nuevos métodos
    public List<Evento> obtenerEventosPorCreador(String usuarioId) {
        return eventoRepository.findByCreadorId(usuarioId);
    }

    public Page<Evento> obtenerEventosPorCreadorId(String usuarioId, Pageable pageable) {
        return eventoRepository.findByCreadorId(usuarioId, pageable);
    }

    public Long contarBoletosVendidosPorCreador(String idCreador) {
        return boletoRepository.countBoletosVendidosPorCreador(idCreador, "ACTIVO");
    }

}
