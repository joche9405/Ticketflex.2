package com.tu_paquete.ticketflex.dto;

import java.math.BigDecimal;

public class PagoRequest {
    private String idBoleto;
    private String metodoPago;

    // NUEVOS CAMPOS NECESARIOS
    private String usuarioId;
    private String eventoId;
    private Integer cantidadBoletos;
    private BigDecimal total;

    // Getters y Setters
    public String getIdBoleto() { return idBoleto; }
    public void setIdBoleto(String idBoleto) { this.idBoleto = idBoleto; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }

    public Integer getCantidadBoletos() { return cantidadBoletos; }
    public void setCantidadBoletos(Integer cantidadBoletos) { this.cantidadBoletos = cantidadBoletos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}

