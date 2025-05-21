/*package com.tu_paquete.ticketflex.Service;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tu_paquete.ticketflex.Model.Transaccion;
import com.tu_paquete.ticketflex.Repository.Mongo.TransaccionRepository;

@Service
public class PagoProgramadoService {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TransaccionRepository transaccionRepository;

    // Ejecutar diariamente a las 6 AM
    @Scheduled(cron = "0 0 6 * * ?")
    public void manejarCuotasPendientes() {
        // 1. Encontrar cuotas que vencen hoy
        LocalDateTime hoy = LocalDateTime.now();
        List<Transaccion> cuotasPorVencer = transaccionRepository
                .findCuotasPendientesPorVencer(hoy.plusDays(1));

        // 2. Procesar cada cuota
        cuotasPorVencer.forEach(cuota -> {
            try {
                // Intento de cobro automático
                transaccionService.procesarPagoCuota(cuota.getId());

                // Notificar al usuario
                emailService.enviarNotificacionPagoExitoso(
                        cuota.getUsuarioId(),
                        cuota);

            } catch (Exception e) {
                // Notificar fallo
                emailService.enviarNotificacionPagoFallido(
                        cuota.getUsuarioId(),
                        cuota,
                        e.getMessage());
            }
        });

        // 3. Recordatorios para cuotas próximas (3 días antes)
        LocalDateTime en3Dias = hoy.plusDays(3);
        List<Transaccion> cuotasProximas = transaccionRepository
                .findCuotasPendientesPorVencer(en3Dias);

        cuotasProximas.forEach(cuota -> {
            emailService.enviarRecordatorioPago(
                    cuota.getUsuarioId(),
                    cuota);
        });
    }

    // En PagoProgramadoService
    @Scheduled(cron = "0 0 6 * * ?")
    public void manejarCuotasPendientes() {
        Date hoy = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(hoy);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date manana = calendar.getTime();

        List<Transaccion> cuotasPorVencer = transaccionRepository
                .findCuotasPendientesPorVencer(manana);

        // Resto del código...
    }
}*/
