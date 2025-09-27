package com.tu_paquete.ticketflex.Model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "perfiles_usuario")
public class PerfilUsuario {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long idPerfil;

    @Column(name = "edad_usuario")
    private Integer edadUsuario;

    @Column(name = "genero_usuario")
    private String generoUsuario;

    @Column(name = "historial_compras_total")
    private Integer historialComprasTotal;

    @Column(name = "frecuencia_visitas")
    private Integer frecuenciaVisitas;

    @Column(name = "tiempo_promedio_navegacion")
    private Double tiempoPromedioNavegacion;

    @Column(name = "interes_principal")
    private String interesPrincipal;

    @Column(name = "dispositivo_predilecto")
    private String dispositivoPredilecto;

    @Column(name = "recibe_notificaciones")
    private Boolean recibeNotificaciones;

    @Column(name = "uso_descuento_previo")
    private Boolean usoDescuentoPrevio; 

}