package com.tu_paquete.ticketflex.Service.dto;

public class NuevaPersonaRequest {
    private int edadUsuario;
    private String generoUsuario;
    private int historialComprasTotal;
    private int frecuenciaVisitas;
    private double tiempoNavegacion;
    private String interesPrincipal;
    private String dispositivoPredilecto;
    private int recibeNotificaciones;
    private int usoDescuentoPrevio;
    private int idFuncionEvento; // ID del evento seleccionado

    // Getters y setters para todos los campos

    

    /**
     * @return int return the edadUsuario
     */
    public int getEdadUsuario() {
        return edadUsuario;
    }

    /**
     * @param edadUsuario the edadUsuario to set
     */
    public void setEdadUsuario(int edadUsuario) {
        this.edadUsuario = edadUsuario;
    }

    /**
     * @return String return the generoUsuario
     */
    public String getGeneroUsuario() {
        return generoUsuario;
    }

    /**
     * @param generoUsuario the generoUsuario to set
     */
    public void setGeneroUsuario(String generoUsuario) {
        this.generoUsuario = generoUsuario;
    }

    /**
     * @return int return the historialComprasTotal
     */
    public int getHistorialComprasTotal() {
        return historialComprasTotal;
    }

    /**
     * @param historialComprasTotal the historialComprasTotal to set
     */
    public void setHistorialComprasTotal(int historialComprasTotal) {
        this.historialComprasTotal = historialComprasTotal;
    }

    /**
     * @return int return the frecuenciaVisitas
     */
    public int getFrecuenciaVisitas() {
        return frecuenciaVisitas;
    }

    /**
     * @param frecuenciaVisitas the frecuenciaVisitas to set
     */
    public void setFrecuenciaVisitas(int frecuenciaVisitas) {
        this.frecuenciaVisitas = frecuenciaVisitas;
    }

    /**
     * @return double return the tiempoNavegacion
     */
    public double getTiempoNavegacion() {
        return tiempoNavegacion;
    }

    /**
     * @param tiempoNavegacion the tiempoNavegacion to set
     */
    public void setTiempoNavegacion(double tiempoNavegacion) {
        this.tiempoNavegacion = tiempoNavegacion;
    }

    /**
     * @return String return the interesPrincipal
     */
    public String getInteresPrincipal() {
        return interesPrincipal;
    }

    /**
     * @param interesPrincipal the interesPrincipal to set
     */
    public void setInteresPrincipal(String interesPrincipal) {
        this.interesPrincipal = interesPrincipal;
    }

    /**
     * @return String return the dispositivoPredilecto
     */
    public String getDispositivoPredilecto() {
        return dispositivoPredilecto;
    }

    /**
     * @param dispositivoPredilecto the dispositivoPredilecto to set
     */
    public void setDispositivoPredilecto(String dispositivoPredilecto) {
        this.dispositivoPredilecto = dispositivoPredilecto;
    }

    /**
     * @return int return the recibeNotificaciones
     */
    public int getRecibeNotificaciones() {
        return recibeNotificaciones;
    }

    /**
     * @param recibeNotificaciones the recibeNotificaciones to set
     */
    public void setRecibeNotificaciones(int recibeNotificaciones) {
        this.recibeNotificaciones = recibeNotificaciones;
    }

    /**
     * @return int return the usoDescuentoPrevio
     */
    public int getUsoDescuentoPrevio() {
        return usoDescuentoPrevio;
    }

    /**
     * @param usoDescuentoPrevio the usoDescuentoPrevio to set
     */
    public void setUsoDescuentoPrevio(int usoDescuentoPrevio) {
        this.usoDescuentoPrevio = usoDescuentoPrevio;
    }

}