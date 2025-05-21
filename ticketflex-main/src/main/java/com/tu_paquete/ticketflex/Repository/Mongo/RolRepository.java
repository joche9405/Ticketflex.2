package com.tu_paquete.ticketflex.Repository.Mongo;

import com.tu_paquete.ticketflex.Model.Rol;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RolRepository extends MongoRepository<Rol, String> {
	Optional<Rol> findByNombreRol(String nombreRol);
    
}
