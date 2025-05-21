package com.tu_paquete.ticketflex.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EventoConEstadisticas {
    private String idEvento;
    private String nombreEvento;
    private LocalDate fecha;
    private Long boletosVendidos;
    private Integer capacidad;
    private BigDecimal ingresos;

    private String categoria;
    private String artista;

    // Constructor
    public EventoConEstadisticas(String idEvento, String nombreEvento, LocalDate fecha,
            Long boletosVendidos, Integer capacidad, BigDecimal ingresos, String categoria, String artista) {
        this.idEvento = idEvento;
        this.nombreEvento = nombreEvento;
        this.fecha = fecha;
        this.boletosVendidos = boletosVendidos != null ? boletosVendidos : 0L;
        this.capacidad = capacidad != null ? capacidad : 0;
        this.ingresos = ingresos != null ? ingresos : BigDecimal.ZERO;
        this.categoria = categoria;
        this.artista = artista;
    }

    // Calcula el porcentaje de ocupación
    public Integer getPorcentajeOcupacion() {
        if (capacidad == null || capacidad == 0 || boletosVendidos == null) {
            return 0;
        }
        return (int) Math.round((boletosVendidos.doubleValue() / capacidad) * 100);
    }

    // Getters
    public String getIdEvento() {
        return idEvento;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Long getBoletosVendidos() {
        return boletosVendidos;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public BigDecimal getIngresos() {
        return ingresos;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getArtista() {
        return artista;
    }
}