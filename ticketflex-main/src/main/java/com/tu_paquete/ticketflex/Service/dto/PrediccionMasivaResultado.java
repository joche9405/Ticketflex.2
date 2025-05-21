package com.tu_paquete.ticketflex.Service.dto;

public class PrediccionMasivaResultado {
    private Long idUsuario;
    private Integer edadUsuario;
    private String generoUsuario;
    private String prediccion;
    private String confianza;
    private String historialCompras;
    private String interesCategoria;

    public PrediccionMasivaResultado(Long idUsuario, Integer edadUsuario, String generoUsuario, String prediccion,
            String confianza, String historialCompras, String interesCategoria) {
        this.idUsuario = idUsuario;
        this.edadUsuario = edadUsuario;
        this.generoUsuario = generoUsuario;
        this.prediccion = prediccion;
        this.confianza = confianza;
        this.historialCompras = historialCompras;
        this.interesCategoria = interesCategoria;
    }

    // Getters y setters
    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getEdadUsuario() {
        return edadUsuario;
    }

    public void setEdadUsuario(Integer edadUsuario) {
        this.edadUsuario = edadUsuario;
    }

    public String getGeneroUsuario() {
        return generoUsuario;
    }

    public void setGeneroUsuario(String generoUsuario) {
        this.generoUsuario = generoUsuario;
    }

    public String getPrediccion() {
        return prediccion;
    }

    public void setPrediccion(String prediccion) {
        this.prediccion = prediccion;
    }

    public String getConfianza() {
        return confianza;
    }

    public void setConfianza(String confianza) {
        this.confianza = confianza;
    }

    public String getHistorialCompras() {
        return historialCompras;
    }

    public void setHistorialCompras(String historialCompras) {
        this.historialCompras = historialCompras;
    }

    public String getInteresCategoria() {
        return interesCategoria;
    }

    public void setInteresCategoria(String interesCategoria) {
        this.interesCategoria = interesCategoria;
    }
}