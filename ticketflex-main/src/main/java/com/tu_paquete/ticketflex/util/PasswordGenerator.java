package com.tu_paquete.ticketflex.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Contraseñas originales
        String passwordAdmin = "admin123";
        String passwordAndres = "admin234";  
        String passwordNicol = "1506";


        // Generar hash de contraseñas
        String hashAdmin = encoder.encode(passwordAdmin);
        String hashAndres = encoder.encode(passwordAndres);
        String hashNicol = encoder.encode(passwordNicol);


        // Imprimir los hashes
        System.out.println("Hash para Admin: " + hashAdmin);
        System.out.println("Hash para Andres: " + hashAndres);
        System.out.println("Hash para Nicol: " + hashNicol);


        // Simulación de verificación de contraseña (ejemplo)
        System.out.println("Verificación Admin: " + encoder.matches(passwordAdmin, hashAdmin));
        System.out.println("Verificación Andres: " + encoder.matches(passwordAndres, hashAndres));
        System.out.println("Verificación Nicol: " + encoder.matches(passwordNicol, hashNicol));

    }
}
