package com.tu_paquete.ticketflex.dto;

import java.time.LocalDate;
import java.util.List;

public class CompraCuotasRequest {
    private String idCuota;
    private String metodoPago;
    private List<DetalleCompraCuotasDTO> detalles;
    private LocalDate fechaEvento;
    private String idUsuario;
    private String idEvento;
    public String getIdEvento() {
		return idEvento;
	}

	public void setIdEvento(String idEvento) {
		this.idEvento = idEvento;
	}

	public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

   
    public CompraCuotasRequest() {}

    public String getIdCuota() {
        return idCuota;
    }

    public void setIdCuota(String idCuota) {
        this.idCuota = idCuota;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<DetalleCompraCuotasDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompraCuotasDTO> detalles) {
        this.detalles = detalles;
    }

    public LocalDate getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDate fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
}

