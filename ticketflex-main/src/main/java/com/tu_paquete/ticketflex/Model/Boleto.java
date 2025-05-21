package com.tu_paquete.ticketflex.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.Date;

@Document(collection = "boletos")
public class Boleto {

    @Id
    private String id;

    private EventoEmbed evento;
    private UsuarioEmbed usuario;

    private BigDecimal precio;
    private Integer cantidad;

    private Date fechaCompra;

    private BigDecimal precioTotal;

    private EstadoBoleto estado;

    private Date fechaLimitePago;

    private String qrCode;

    private BigDecimal saldoPendiente;

    public enum EstadoBoleto {
        PENDIENTE, ACTIVO, CANCELADO
    }

    // ==== Embedded classes (con constructores agregados) ====

    public static class EventoEmbed {
        private String id;
        private String nombre;
        private Date fecha;
        private String idUsuario;

        // ✅ Constructor con parámetros
        public EventoEmbed(String id, String nombre, Date fecha, String idUsuario) {
            this.id = id;
            this.nombre = nombre;
            this.fecha = fecha;
            this.idUsuario = idUsuario;
        }

        public EventoEmbed() {}

        // Getters y Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public Date getFecha() { return fecha; }
        public void setFecha(Date fecha) { this.fecha = fecha; }

        public String getIdUsuario() { return idUsuario; }
        public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    }

    public static class UsuarioEmbed {
        private String id;
        private String nombre;
        private String correo;

        // ✅ Constructor con parámetros
        public UsuarioEmbed(String id, String nombre, String correo) {
            this.id = id;
            this.nombre = nombre;
            this.correo = correo;
        }

        public UsuarioEmbed() {}

        // Getters y Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
    }

    // ==== Getters y Setters del documento Boleto ====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public EventoEmbed getEvento() { return evento; }
    public void setEvento(EventoEmbed evento) { this.evento = evento; }

    public UsuarioEmbed getUsuario() { return usuario; }
    public void setUsuario(UsuarioEmbed usuario) { this.usuario = usuario; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Date getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(Date fechaCompra) { this.fechaCompra = fechaCompra; }

    public BigDecimal getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; }

    public EstadoBoleto getEstado() { return estado; }
    public void setEstado(EstadoBoleto estado) { this.estado = estado; }

    public Date getFechaLimitePago() { return fechaLimitePago; }
    public void setFechaLimitePago(Date fechaLimitePago) { this.fechaLimitePago = fechaLimitePago; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    public String getUsuarioId() {
        return (usuario != null) ? usuario.getId() : null;
    }

    public String getEventoId() {
        return (evento != null) ? evento.getId() : null;
    }
}
