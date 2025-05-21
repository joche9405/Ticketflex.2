package com.tu_paquete.ticketflex.dto;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Transaccion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PurchaseDTO {
    private String idTransaccion;
    private String idBoleto;
    private String nombreEvento;
    private Date fechaCompra;
    private BigDecimal total;
    private String estado;
    private String qrCode;
    private Date fechaEvento;
    private String lugarEvento;
    private List<BoletoDetalleDTO> boletos = new ArrayList<>();

    // DTO para los detalles de cada boleto
    public static class BoletoDetalleDTO {
        private Integer cantidad;
        private String tipo;
        private String graderia;
        private BigDecimal precio;

        public BoletoDetalleDTO(Integer cantidad, String tipo, String graderia, BigDecimal precio) {
            this.cantidad = cantidad;
            this.tipo = tipo;
            this.graderia = graderia;
            this.precio = precio;
        }

        // Getters y setters
        public Integer getCantidad() {
            return cantidad;
        }

        public String getTipo() {
            return tipo;
        }

        public String getGraderia() {
            return graderia;
        }

        public BigDecimal getPrecio() {
            return precio;
        }
    }

    public PurchaseDTO(Transaccion transaccion, Boleto boleto, Evento evento) {
        this.idTransaccion = transaccion.getId() != null ? transaccion.getId().toHexString() : null;
        this.idBoleto = boleto != null ? boleto.getId() : null;
        this.nombreEvento = evento != null ? evento.getNombreEvento() : "Evento desconocido";
        this.fechaCompra = transaccion.getFechaPago();
        this.total = transaccion.getTotal();
        this.estado = boleto != null && boleto.getEstado() != null
                ? boleto.getEstado().name()
                : (transaccion.getEstadoPago() != null ? transaccion.getEstadoPago() : "Sin estado");
        this.qrCode = boleto != null ? boleto.getQrCode() : null;
        this.fechaEvento = evento != null ? java.sql.Date.valueOf(evento.getFecha()) : null;
        this.lugarEvento = evento != null ? evento.getLugar() : null;

        // Detalle del boleto (si solo tienes uno asociado a esta transacción)
        if (boleto != null) {
            this.boletos.add(new BoletoDetalleDTO(
                    boleto.getCantidad(),
                    "General", // O el tipo real si lo tienes
                    boleto.getEvento() != null ? boleto.getEvento().getNombre() : null, // O gradería real
                    boleto.getPrecio()));
        }
    }

    // Getters
    public String getIdTransaccion() {
        return idTransaccion;
    }

    public String getIdBoleto() {
        return idBoleto;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public Date getFechaCompra() {
        return fechaCompra;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    public String getQrCode() {
        return qrCode;
    }

    public Date getFechaEvento() {
        return fechaEvento;
    }

    public String getLugarEvento() {
        return lugarEvento;
    }

    public List<BoletoDetalleDTO> getBoletos() {
        return boletos;
    }
}
