package com.tu_paquete.ticketflex.dto;

public class PagoTarjetaRequest {
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    // ✅ Asegúrate de tener estos getters
    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    // ✅ Y estos setters (importantes para Jackson)
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}