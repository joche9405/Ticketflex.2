package com.tu_paquete.ticketflex.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tu_paquete.ticketflex.Model.Boleto;
import com.tu_paquete.ticketflex.Model.PagoCuota;
import com.tu_paquete.ticketflex.Model.Usuario;
import com.tu_paquete.ticketflex.Service.PagoCuotaService;
import com.tu_paquete.ticketflex.Service.PayUService;
import com.tu_paquete.ticketflex.Service.UsuarioService;
import com.tu_paquete.ticketflex.dto.CompraCuotasRequest;

@RestController
@RequestMapping("/api/pagos-cuotas")
public class PagoCuotaController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PagoCuotaService pagoCuotaService;

    @Autowired
    private PayUService payUService;

    public PagoCuotaController(PagoCuotaService pagoCuotaService, UsuarioService usuarioService,
            PayUService payUService) {
        this.pagoCuotaService = pagoCuotaService;
        this.usuarioService = usuarioService;
        this.payUService = payUService;
    }

    private boolean permitePagoCuotas(LocalDate fechaEvento) {
        LocalDate hoy = LocalDate.now();
        long diasDiferencia = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaEvento);
        return diasDiferencia > 30;
    }

    @PostMapping("/generar")
    public ResponseEntity<?> generarCuotasYPrimerPago(@RequestBody CompraCuotasRequest request) {
        try {
            if (request.getFechaEvento() == null) {
                return ResponseEntity.badRequest().body("La fecha del evento es obligatoria.");
            }

            if (request.getIdEvento() == null || request.getIdEvento().isBlank()) {
                return ResponseEntity.badRequest().body("El ID del evento es obligatorio.");
            }

            LocalDate fechaEvento = request.getFechaEvento();

            if (!permitePagoCuotas(fechaEvento)) {
                return ResponseEntity.badRequest()
                        .body("No se puede pagar a cuotas porque el evento es en 30 días o menos.");
            }

            List<PagoCuota> cuotas = pagoCuotaService.generarCuotasDeCompra(request, fechaEvento);

            if (cuotas.isEmpty()) {
                return ResponseEntity.badRequest().body("No se generaron cuotas.");
            }

            PagoCuota primeraCuota = cuotas.get(0);

            Usuario usuario = usuarioService.buscarPorId(primeraCuota.getUsuarioId());
            if (usuario == null) {
                return ResponseEntity.badRequest().body("Usuario no encontrado para la cuota.");
            }

            Boleto boletoTemporal = new Boleto();
            boletoTemporal.setId("CUOTA_" + primeraCuota.getId());
            boletoTemporal.setPrecioTotal(primeraCuota.getValor());
            boletoTemporal.setId(request.getIdEvento());
            boletoTemporal.setUsuario(new Boleto.UsuarioEmbed(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getEmail()));

            String formularioPayU = payUService.generarFormularioRedireccion(boletoTemporal);

            return ResponseEntity.ok(Map.of(
                    "cuotas", cuotas,
                    "formulario", formularioPayU));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor");
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<PagoCuota>> cuotasUsuario(@PathVariable String idUsuario) {
        List<PagoCuota> cuotas = pagoCuotaService.obtenerCuotasPorUsuario(idUsuario);
        return ResponseEntity.ok(cuotas);
    }

    @PostMapping("/liberar-boletas-vencidas")
    public ResponseEntity<String> liberarBoletasVencidas() {
        pagoCuotaService.liberarBoletasNoPagadas();
        return ResponseEntity.ok("Boletas vencidas liberadas");
    }

    @PostMapping("/cuota/{id}/pagar")
    public ResponseEntity<String> pagarCuota(@PathVariable String id) {
        boolean pagada = pagoCuotaService.marcarComoPagada(id);
        if (pagada) {
            return ResponseEntity.ok("Cuota pagada exitosamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cuota no encontrada");
        }
    }

    @GetMapping("/cuotas/{idBoleto}")
    public ResponseEntity<List<PagoCuota>> listarCuotasPorBoleto(@PathVariable String idBoleto) {
        return ResponseEntity.ok(pagoCuotaService.obtenerCuotasPorBoleto(idBoleto));
    }
}
