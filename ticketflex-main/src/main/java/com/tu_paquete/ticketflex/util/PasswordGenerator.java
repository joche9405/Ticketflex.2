package com.tu_paquete.ticketflex.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Contraseñas originales
        String passwordAdmin = "admin123";
        String passwordZaida = "admin234";
        String passwordNicol = "1506";

        // Generar hash de contraseñas
        String hashAdmin = encoder.encode(passwordAdmin);
        String hashZaida = encoder.encode(passwordZaida);
        String hashNicol = encoder.encode(passwordNicol);

        // Imprimir los hashes
        System.out.println("Hash para Admin: " + hashAdmin);
        System.out.println("Hash para Zaida: " + hashZaida);
        System.out.println("Hash para Nicol: " + hashNicol);

        // Simulación de verificación de contraseña (ejemplo)
        System.out.println("Verificación Admin: " + encoder.matches(passwordAdmin, hashAdmin));
        System.out.println("Verificación Zaida: " + encoder.matches(passwordZaida, hashZaida));
        System.out.println("Verificación Nicol: " + encoder.matches(passwordNicol, hashNicol));

        // 🔹 Generar un secret aleatorio de 256 bits para JWT (32 bytes)
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(keyBytes);
        String jwtSecret = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("🔑 Nuevo JWT Secret (256 bits): " + jwtSecret);
    }
}
