package com.tu_paquete.ticketflex.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException; // Importar específicamente MailException
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarCorreo(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // `true` indica que el cuerpo es HTML

            mailSender.send(message);
            log.info("Correo enviado exitosamente a: {}", to);

        } catch (MailException e) {
            // **MANEJO DE FALLOS DE CONEXIÓN SMTP (CRUCIAL)**
            // Captura el fallo de envío, lo registra, y PERMITE que el programa continúe.
            log.error("Error de conexión/envío de correo a {}. La compra se completó, pero el correo falló: {}",
                    to, e.getMessage());
            // No se lanza ninguna excepción (no 'throw'), permitiendo que el controlador
            // devuelva 200 OK.

        } catch (Exception e) {
            // Manejo de cualquier otro fallo (p.ej., problemas con MimeMessageHelper)
            log.error("Error inesperado al construir/enviar el correo a {}: {}", to, e.getMessage());
        }
    }
}
