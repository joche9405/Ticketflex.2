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

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String rol = auth.getAuthority();

            if ("ROLE_Administrador".equals(rol)) {
                response.sendRedirect("/admin/dashboard");
                return;
            } else if ("ROLE_Usuario".equals(rol)) {
                response.sendRedirect("/usuario/index");
                return;
            }
        }

        // Si no hay rol válido, redirige al index por defecto
        response.sendRedirect("/");
    }
}