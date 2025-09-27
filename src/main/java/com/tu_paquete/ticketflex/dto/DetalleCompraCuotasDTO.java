package com.tu_paquete.ticketflex.dto;

import java.math.BigDecimal;

public class DetalleCompraCuotasDTO {
    private String idUsuario;
    private String idEvento;
    private BigDecimal valor;
    private int cantidad;
    public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public DetalleCompraCuotasDTO() {}

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(String idEvento) {
        this.idEvento = idEvento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
