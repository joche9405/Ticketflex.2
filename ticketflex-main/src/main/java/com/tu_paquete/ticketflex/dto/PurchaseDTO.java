package com.tu_paquete.ticketflex.dto;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.Evento;
import com.tu_paquete.ticketflex.Model.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private LocalDate fechaEvento;
    private String lugarEvento;
    private List<BoletoDetalleDTO> boletos = new ArrayList<>();
    private String metodoPago;
    private LocalDate fechaLimitePago;
    private LocalDate fechaProximoPago;

    // DTO para los detalles de cada boleto
    public static class BoletoDetalleDTO {
        private Integer cantidad;
        private String graderia;
        private BigDecimal precioUnitario;
        private BigDecimal precioTotal;

        public BoletoDetalleDTO(Integer cantidad, String graderia, BigDecimal precioUnitario) {
            this.cantidad = cantidad != null ? cantidad : 1;
            this.graderia = graderia != null ? graderia : "General";
            this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
            this.precioTotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public String getGraderia() {
            return graderia;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public BigDecimal getPrecioTotal() {
            return precioTotal;
        }
    }

    public PurchaseDTO(Transaccion transaccion, Boleto boleto, Evento evento) {
        this.idTransaccion = transaccion.getId() != null ? transaccion.getId().toString() : null;
        this.idBoleto = boleto != null ? boleto.getId() : null;
        this.nombreEvento = evento != null && evento.getNombreEvento() != null ? evento.getNombreEvento()
                : "Evento desconocido";
        this.fechaCompra = transaccion.getFechaPago();
        this.total = transaccion.getTotal() != null ? transaccion.getTotal() : BigDecimal.ZERO;
        this.estado = boleto != null && boleto.getEstado() != null
                ? boleto.getEstado().name()
                : (transaccion.getEstadoPago() != null ? transaccion.getEstadoPago() : "DESCONOCIDO");
        this.qrCode = boleto != null && boleto.getQrCode() != null ? boleto.getQrCode() : "Sin código";
        this.fechaEvento = evento != null ? evento.getFecha() : null;
        this.lugarEvento = evento != null && evento.getLugar() != null ? evento.getLugar() : "Por definir";

        this.metodoPago = transaccion.getMetodoPago() != null ? transaccion.getMetodoPago() : "N/A";
        this.fechaLimitePago = transaccion.getFechaLimitePago();
        this.fechaProximoPago = transaccion.getFechaProximoPago();

        // Detalle del boleto: solo gradería "General"
        if (boleto != null) {
            this.boletos.add(new BoletoDetalleDTO(
                    boleto.getCantidad(),
                    "General",
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

    public LocalDate getFechaEvento() {
        return fechaEvento;
    }

    public String getLugarEvento() {
        return lugarEvento;
    }

    public List<BoletoDetalleDTO> getBoletos() {
        return boletos;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDate getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(LocalDate fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }

    public LocalDate getFechaProximoPago() {
        return fechaProximoPago;
    }

    public void setFechaProximoPago(LocalDate fechaProximoPago) {
        this.fechaProximoPago = fechaProximoPago;
    }
}
