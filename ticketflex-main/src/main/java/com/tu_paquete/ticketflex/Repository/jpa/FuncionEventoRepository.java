package com.tu_paquete.ticketflex.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tu_paquete.ticketflex.Model.FuncionEvento;

@Repository
public interface FuncionEventoRepository extends JpaRepository<FuncionEvento, Long> {

}
