package com.tu_paquete.ticketflex.Model;

public class PrediccionResultado {
    private String prediccion;
    private String confianza;

    public PrediccionResultado() {
    }

    public PrediccionResultado(String prediccion, String confianza) {
        this.prediccion = prediccion;
        this.confianza = confianza;
    }

    // Getters y setters para prediccion y confianza

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
}