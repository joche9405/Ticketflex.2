package com.tu_paquete.ticketflex.dto;

import java.time.Instant;

public class RespuestaAPI {
    private String mensaje;
    private Instant timestamp;
    private Object data;  // Opcional: para incluir datos adicionales

    public RespuestaAPI(String mensaje) {
        this.mensaje = mensaje;
        this.timestamp = Instant.now();
    }

    public RespuestaAPI(String mensaje, Object data) {
        this.mensaje = mensaje;
        this.data = data;
        this.timestamp = Instant.now();
    }

    // Getters (sin setters para inmutabilidad)
    public String getMensaje() {
        return mensaje;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Object getData() {
        return data;
    }
}