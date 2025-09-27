package com.tu_paquete.ticketflex.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pagos_cuotas")
public class PagoCuota {

    @Id
    private String id;
    private String idCompra;

    @DBRef 
    private Usuario usuario;

    private String idEvento;
    private String idBoleto; // Relacionar con boleta comprada
    private BigDecimal valor;
    private String usuarioId;

    

    private int numeroCuota; // Ej: cuota 1 de 6
    private BigDecimal montoCuota;

    private LocalDate fechaVencimiento;
    private boolean pagado;
    private String estado;
    private LocalDate fechaPago; // si se pagó
    private String metodoPago; // "TicketFlex", "PayU", etc.
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	public String getIdEvento() {
		return idEvento;
	}
	public void setIdEvento(String idEvento) {
		this.idEvento = idEvento;
	}
	public String getIdBoleto() {
		return idBoleto;
	}
	public void setIdBoleto(String idBoleto) {
		this.idBoleto = idBoleto;
	}
	public int getNumeroCuota() {
		return numeroCuota;
	}
	public void setNumeroCuota(int numeroCuota) {
		this.numeroCuota = numeroCuota;
	}


	public BigDecimal getMontoCuota() {
		return montoCuota;
	}
	public void setMontoCuota(BigDecimal montoCuota) {
		this.montoCuota = montoCuota;
	}
	public LocalDate getFechaVencimiento() {
		return fechaVencimiento;
	}
	public void setFechaVencimiento(LocalDate fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}
	public boolean isPagado() {
		return pagado;
	}
	public void setPagado(boolean pagado) {
		this.pagado = pagado;
	}
	public LocalDate getFechaPago() {
		return fechaPago;
	}
	public void setFechaPago(LocalDate fechaPago) {
		this.fechaPago = fechaPago;
	}
	public String getMetodoPago() {
		return metodoPago;
	}
	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}
	public String getIdCompra() {
		return idCompra;
	}
	public void setIdCompra(String idCompra) {
		this.idCompra = idCompra;
	}
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
    
	public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
    
}

