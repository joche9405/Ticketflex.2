package com.tu_paquete.ticketflex.Model;

import java.math.BigDecimal;

public class Graderia {
    private String nombre;
    private BigDecimal precio;
    private Integer disponibilidad;

    // Constructor vacío
    public Graderia() {}

    // Constructor con parámetros
    public Graderia(String nombre, BigDecimal precio, Integer disponibilidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.disponibilidad = disponibilidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(Integer disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
}
