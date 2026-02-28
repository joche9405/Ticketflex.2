package com.tu_paquete.ticketflex.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "boletos")
public class Boleto {

    @Id
    private String id;

    private EventoEmbed evento;
    private UsuarioEmbed usuario;

    private BigDecimal precio;
    private Integer cantidad;

    private LocalDate fechaCompra;
    private BigDecimal precioTotal;
    private EstadoBoleto estado;
    private LocalDate fechaLimitePago;
    private String qrCode;
    private BigDecimal saldoPendiente;

    // ✅ CAMPOS NUEVOS PARA TICKETFLEX
    private String graderia; // Tipo de gradería (general, vip, etc.)
    private Integer cuotas; // Número de cuotas para TicketFlex
    private String metodoPago; // TICKETFLEX, TRADICIONAL, etc.
    private String nombreBoleta; // Nombre descriptivo de la boleta
    private LocalDate fechaProximoPago;

    public enum EstadoBoleto {
        PENDIENTE, ACTIVO, CANCELADO, PAGADO_PARCIALMENTE
    }

    public static class EventoEmbed {
        private String id;
        private String nombre;
        private LocalDate fecha;
        private String idUsuario;

        public EventoEmbed() {
        }

        public EventoEmbed(String id, String nombre, LocalDate fecha, String idUsuario) {
            this.id = id;
            this.nombre = nombre;
            this.fecha = fecha;
            this.idUsuario = idUsuario;
        }

        // Getters y Setters...
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public String getIdUsuario() {
            return idUsuario;
        }

        public void setIdUsuario(String idUsuario) {
            this.idUsuario = idUsuario;
        }

    }

    public static class UsuarioEmbed {
        private String id;
        private String nombre;
        private String correo;

        public UsuarioEmbed() {
        }

        public UsuarioEmbed(String id, String nombre, String correo) {
            this.id = id;
            this.nombre = nombre;
            this.correo = correo;
        }

        // Getters y Setters...
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getCorreo() {
            return correo;
        }

        public void setCorreo(String correo) {
            this.correo = correo;
        }
    }

    // ==== GETTERS Y SETTERS ====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventoEmbed getEvento() {
        return evento;
    }

    public void setEvento(EventoEmbed evento) {
        this.evento = evento;
    }

    public UsuarioEmbed getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEmbed usuario) {
        this.usuario = usuario;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public EstadoBoleto getEstado() {
        return estado;
    }

    public void setEstado(EstadoBoleto estado) {
        this.estado = estado;
    }

    public LocalDate getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(LocalDate fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    // ✅ GETTERS Y SETTERS PARA LOS CAMPOS NUEVOS

    public String getGraderia() {
        return graderia;
    }

    public void setGraderia(String graderia) {
        this.graderia = graderia;
    }

    public Integer getCuotas() {
        return cuotas;
    }

    public void setCuotas(Integer cuotas) {
        this.cuotas = cuotas;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNombreBoleta() {
        return nombreBoleta;
    }

    public void setNombreBoleta(String nombreBoleta) {
        this.nombreBoleta = nombreBoleta;
    }

    public String getUsuarioId() {
        return (usuario != null) ? usuario.getId() : null;
    }

    public String getEventoId() {
        return (evento != null) ? evento.getId() : null;
    }

    // ✅ MÉTODO PARA GENERAR NOMBRE DE BOLETA AUTOMÁTICO
    public String generarNombreBoleta() {
        if (graderia != null && evento != null) {
            return evento.getNombre() + " - " + graderia.toUpperCase();
        }
        return "Boleto General";
    }

    public LocalDate getFechaProximoPago() {
        return fechaProximoPago;
    }

    public void setFechaProximoPago(LocalDate fechaProximoPago) {
        this.fechaProximoPago = fechaProximoPago;
    }
}