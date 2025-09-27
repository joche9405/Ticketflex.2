package com.tu_paquete.ticketflex.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "transacciones")
public class Transaccion {

    @Id
    private String id;

    private String boletoId; // referencia al id de Boleto (String)
    private String usuarioId; // referencia al id de Usuario (String)
    private String eventoId; // referencia al id de Evento (String)

    private Integer cantidadBoletos;
    private BigDecimal total;
    private Integer numeroCuotas;
    private String estadoPago; // PENDIENTE, EXITOSO, CANCELADO, etc.
    private Date fechaPago;
    private String payuTransactionId; // ID que devuelve PayU
    private String metodoPago; // “debito”, “credito”, “ticketflex”
    private String tipo; // "PagoTotal", "PagoTicketFlexCuota", etc.
    private BigDecimal montoPagado;
    private BigDecimal saldoRestante;
    private LocalDateTime fecha;
    private String referenceCode;
    private String emailComprador;
    private LocalDate fechaLimitePago;
    private LocalDate fechaProximoPago;

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public BigDecimal getSaldoRestante() {
        return saldoRestante;
    }

    public void setSaldoRestante(BigDecimal saldoRestante) {
        this.saldoRestante = saldoRestante;
    }

    public Transaccion() {
    }

    // ==== Getters & Setters ====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoletoId() {
        return boletoId;
    }

    public void setBoletoId(String boletoId) {
        this.boletoId = boletoId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public Integer getCantidadBoletos() {
        return cantidadBoletos;
    }

    public void setCantidadBoletos(Integer cantidadBoletos) {
        this.cantidadBoletos = cantidadBoletos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getNumeroCuotas() {
        return numeroCuotas;
    }

    public void setNumeroCuotas(Integer numeroCuotas) {
        this.numeroCuotas = numeroCuotas;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getPayuTransactionId() {
        return payuTransactionId;
    }

    public void setPayuTransactionId(String payuTransactionId) {
        this.payuTransactionId = payuTransactionId;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getEmailComprador() {
        return emailComprador;
    }

    public void setEmailComprador(String emailComprador) {
        this.emailComprador = emailComprador;
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

    public Transaccion(String id, String boletoId, String usuarioId, String eventoId, Integer cantidadBoletos,
            BigDecimal total, Integer numeroCuotas, String estadoPago, Date fechaPago, String payuTransactionId,
            String metodoPago) {
        super();
        this.id = id;
        this.boletoId = boletoId;
        this.usuarioId = usuarioId;
        this.eventoId = eventoId;
        this.cantidadBoletos = cantidadBoletos;
        this.total = total;
        this.numeroCuotas = numeroCuotas;
        this.estadoPago = estadoPago;
        this.fechaPago = fechaPago;
        this.payuTransactionId = payuTransactionId;
        this.metodoPago = metodoPago;
    }

}
