package com.tu_paquete.ticketflex.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "eventos")
public class Evento {

    @Id
    private String id;

    private Usuario creador; // Usuario embebido

    private String nombreEvento;
    private LocalDate fecha;
    private String lugar;
    private String descripcion;
    private BigDecimal precioBase;
    private BigDecimal precioVIP;
    private Integer disponibilidad;
    @Field("imagen")
    private String imagen;
    private String categoria;
    private String artista;

    public BigDecimal getPrecioVIP() {
        return precioVIP;
    }

    public void setPrecioVIP(BigDecimal precioVIP) {
        this.precioVIP = precioVIP;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Usuario getCreador() {
        return creador;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public void setNombreEvento(String nombreEvento) {
        this.nombreEvento = nombreEvento;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public Integer getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(Integer disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public String getImagenUrl() {
        return "/api/imagen/" + getImagen();
    } public String getImagen() {
        return imagen != null ? imagen : "default.jpg";
    }


    public void setImagen(String imagen) {
        this.imagen = imagen != null ? imagen : "default.jpg";
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }
}
