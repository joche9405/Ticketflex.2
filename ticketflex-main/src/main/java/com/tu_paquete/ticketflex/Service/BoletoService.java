package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Repository.Mongo.BoletoRepository;
import com.tu_paquete.ticketflex.Repository.Mongo.EventoRepository;
import com.tu_paquete.ticketflex.Repository.Mongo.UsuarioRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class BoletoService {

    @Autowired
    private BoletoRepository boletoRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransaccionService transaccionService;

    public Boleto obtenerPorId(String id) {
        return boletoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boleto no encontrado"));
    }

    public Boleto comprarBoleto(String idEvento, String idUsuario, Integer cantidad) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (evento.getDisponibilidad() < cantidad) {
            throw new RuntimeException("No hay suficiente disponibilidad para este evento");
        }

        Boleto boleto = new Boleto();
        Boleto.EventoEmbed evEmb = new Boleto.EventoEmbed();
        evEmb.setId(evento.getId().toString());
        evEmb.setNombre(evento.getNombreEvento());
        boleto.setEvento(evEmb);
        Boleto.UsuarioEmbed uEmb = new Boleto.UsuarioEmbed();
        uEmb.setId(usuario.getId());
        uEmb.setNombre(usuario.getNombre());
        uEmb.setCorreo(usuario.getEmail());
        boleto.setUsuario(uEmb);
        boleto.setFechaCompra(new Date());
        boleto.setCantidad(cantidad);
        boleto.setPrecio(evento.getPrecioBase());
        boleto.setPrecioTotal(evento.getPrecioBase().multiply(BigDecimal.valueOf(cantidad)));

        Boleto boletoGuardado = boletoRepository.save(boleto);

        evento.setDisponibilidad(evento.getDisponibilidad() - cantidad);
        eventoRepository.save(evento);

        Transaccion trx = new Transaccion();
        trx.setBoletoId(boletoGuardado.getId());
        trx.setUsuarioId(usuario.getId());
        trx.setEventoId(evento.getId().toString());
        trx.setCantidadBoletos(cantidad);
        trx.setTotal(boletoGuardado.getPrecioTotal());
        trx.setFechaPago(new Date());
        trx.setEstadoPago("COMPLETADO");
        transaccionService.crearTransaccion(trx);

        return boletoGuardado;
    }

    public List<Boleto> findByUsuarioId(String usuarioId) {
    return boletoRepository.findByUsuario_Id(usuarioId);  // <-- importante: usar _ si usas objetos embebidos
}

}
