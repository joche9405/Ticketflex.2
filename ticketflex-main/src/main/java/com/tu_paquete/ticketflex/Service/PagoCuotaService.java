package com.tu_paquete.ticketflex.Service;

import java.time.LocalDate;
import java.util.List;

import com.tu_paquete.ticketflex.Model.PagoCuota;
import com.tu_paquete.ticketflex.dto.CompraCuotasRequest;

public interface PagoCuotaService {
    PagoCuota guardarPagoCuota(PagoCuota pagoCuota);
    List<PagoCuota> obtenerTodos();
    PagoCuota obtenerPorId(String id);
    PagoCuota eliminarPorId(String id);
    
    List<PagoCuota> generarCuotasDeCompra(CompraCuotasRequest request, LocalDate fechaEvento);
    
    void liberarBoletasNoPagadas();
    List<PagoCuota> obtenerCuotasPorUsuario(String idUsuario);
    boolean marcarComoPagada(String idCuota);
    List<PagoCuota> obtenerCuotasPorBoleto(String idBoleto);
    void actualizarEstadosCuotasVencidas();


    
}

