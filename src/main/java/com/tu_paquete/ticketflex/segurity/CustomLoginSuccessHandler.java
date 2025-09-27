package com.tu_paquete.ticketflex.segurity;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        System.out.println("=== DEBUG: USUARIO AUTENTICADO ===");
        System.out.println("Nombre: " + authentication.getName());
        System.out.println("Roles: " + authentication.getAuthorities());

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String rol = auth.getAuthority();
            System.out.println("Rol encontrado: " + rol);

            if ("ROLE_Administrador".equals(rol)) {
                System.out.println("Redirigiendo a /admin/dashboard");
                response.sendRedirect("/admin/dashboard");
                return;
            } else if ("ROLE_Usuario".equals(rol)) {
                System.out.println("Redirigiendo a /usuario/index");
                response.sendRedirect("/usuario/index");
                return;
            }
        }

        System.out.println("No se encontraron roles válidos, redirigiendo a /");
        response.sendRedirect("/");
    }
}