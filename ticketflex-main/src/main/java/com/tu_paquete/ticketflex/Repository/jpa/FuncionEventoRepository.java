package com.tu_paquete.ticketflex.Repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tu_paquete.ticketflex.Model.FuncionEvento;


@Repository
public interface FuncionEventoRepository extends JpaRepository<FuncionEvento, Long> {
    // Spring Data JPA proporciona métodos como findById, findAll, save, delete, etc.
    // Puedes agregar métodos personalizados aquí si necesitas consultas más específicas.
}
