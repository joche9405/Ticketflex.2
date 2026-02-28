package com.tu_paquete.ticketflex.Model;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
@Table(name = "funciones_evento")
public class FuncionEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcion")
    private Long idFuncion;

    @Column(name = "tipo_funcion")
    private String tipoFuncion;

    @Column(name = "nombre_funcion")
    private String nombreFuncion;

    @Column(name = "fecha_funcion")
    @Temporal(TemporalType.DATE)
    private Date fechaFuncion;

    @Column(name = "lugar_funcion")
    private String lugarFuncion;

}