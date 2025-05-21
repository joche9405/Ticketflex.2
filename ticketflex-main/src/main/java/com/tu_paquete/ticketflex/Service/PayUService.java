package com.tu_paquete.ticketflex.Service;

import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.PagoCuota;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class PayUService {

    private static final String API_KEY = "4Vj8eK4rloUd272L48hsrarnUA";
    private static final String MERCHANT_ID = "508029";
    private static final String ACCOUNT_ID = "512321";
    private static final String URL_PAYU_SANDBOX = "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/";
    private static final String CURRENCY = "COP";
    private static final String RESPONSE_URL = "https://4d26-181-68-224-116.ngrok-free.app/respuesta-payu";
    private static final String CONFIRMATION_URL = "https://4d26-181-68-224-116.ngrok-free.app/confirmacion-payu";


    public String generarFormularioRedireccion(Boleto boleto) {
        String referenceCode = "BOLETO_" + boleto.getId();
        String amount = boleto.getPrecioTotal().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String signature = generarFirma(API_KEY, MERCHANT_ID, referenceCode, amount, CURRENCY);
        System.out.println("Monto enviado: " + amount);
        System.out.println("Firma generada: " + signature);
        System.out.println("Código de referencia: " + referenceCode);
        System.out.println("Correo del comprador: " + boleto.getUsuario().getCorreo());
        System.out.println("URL de respuesta: " + RESPONSE_URL);
        System.out.println("URL de confirmación: " + CONFIRMATION_URL);
        return "<html>" +
                "<body onload='document.forms[\"payuForm\"].submit()'>" +
                "<form name='payuForm' method='POST' action='" + URL_PAYU_SANDBOX + "'>" +
                "<input name='merchantId'    type='hidden'  value='" + MERCHANT_ID + "'>" +
                "<input name='accountId'     type='hidden'  value='" + ACCOUNT_ID + "'>" +
                "<input name='description'   type='hidden'  value='Compra de boleto'>" +
                "<input name='referenceCode' type='hidden'  value='" + referenceCode + "'>" +
                "<input name='amount'        type='hidden'  value='" + amount + "'>" +
                "<input name='currency'      type='hidden'  value='" + CURRENCY + "'>" +
                "<input name='signature'     type='hidden'  value='" + signature + "'>" +
                "<input name='buyerEmail'    type='hidden'  value='" + boleto.getUsuario().getCorreo() + "'>" +
                "<input name='responseUrl'   type='hidden'  value='" + RESPONSE_URL + "'>" +
                "<input name='confirmationUrl' type='hidden' value='" + CONFIRMATION_URL + "'>" +
                "<input name='test'          type='hidden'  value='1'>" +
                "</form>" +
                "</body>" +
                "</html>";
    }


    public String generarFirma(String apiKey, String merchantId, String referenceCode, String amount, String currency) {
        try {
            String plainText = apiKey + "~" + merchantId + "~" + referenceCode + "~" + amount + "~" + currency;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generando firma", e);
        }
    }
    public String generarFormularioRedireccion(PagoCuota cuota) {
        String referenceCode = "CUOTA_" + cuota.getId();
        String amount = cuota.getMontoCuota().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String signature = generarFirma(API_KEY, MERCHANT_ID, referenceCode, amount, CURRENCY);

        System.out.println("Monto enviado: " + amount);
        System.out.println("Firma generada: " + signature);
        System.out.println("Código de referencia: " + referenceCode);
        System.out.println("Correo del comprador: " + cuota.getUsuario().getEmail());

        return "<html>" +
                "<body onload='document.forms[\"payuForm\"].submit()'>" +
                "<form name='payuForm' method='POST' action='" + URL_PAYU_SANDBOX + "'>" +
                "<input name='merchantId'    type='hidden'  value='" + MERCHANT_ID + "'>" +
                "<input name='accountId'     type='hidden'  value='" + ACCOUNT_ID + "'>" +
                "<input name='description'   type='hidden'  value='Pago de primera cuota'>" +
                "<input name='referenceCode' type='hidden'  value='" + referenceCode + "'>" +
                "<input name='amount'        type='hidden'  value='" + amount + "'>" +
                "<input name='currency'      type='hidden'  value='" + CURRENCY + "'>" +
                "<input name='signature'     type='hidden'  value='" + signature + "'>" +
                "<input name='buyerEmail'    type='hidden'  value='" + cuota.getUsuario().getEmail() + "'>" +
                "<input name='responseUrl'   type='hidden'  value='" + RESPONSE_URL + "'>" +
                "<input name='confirmationUrl' type='hidden' value='" + CONFIRMATION_URL + "'>" +
                "<input name='test'          type='hidden'  value='1'>" +
                "</form>" +
                "</body>" +
                "</html>";
    }


}

