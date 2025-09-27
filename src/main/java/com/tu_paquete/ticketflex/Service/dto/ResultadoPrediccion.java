package com.tu_paquete.ticketflex.Service.dto;

public class ResultadoPrediccion {
    private String prediccion;
    private String confianza;

    public ResultadoPrediccion(String prediccion, String confianza) {
        this.prediccion = prediccion;
        this.confianza = confianza;
    }

    public String getPrediccion() {
        return prediccion;
    }

    public String getConfianza() {
        return confianza;
    }

    public void setPrediccion(String prediccion) {
        this.prediccion = prediccion;
    }

    public void setConfianza(String confianza) {
        this.confianza = confianza;
    }
}