package com.tu_paquete.ticketflex.segurity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections; // Usa el java.util estándar

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.validarToken(token)) {
                String userId = jwtUtil.obtenerUserId(token);
                String rol = jwtUtil.obtenerRol(token);

                // 1. Creamos la autoridad (Spring espera ROLE_ al usar hasRole en
                // SecurityConfig)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol);

                // 2. Creamos el objeto de autenticación para Spring Security
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,
                        null, Collections.singletonList(authority));

                // 3. ¡IMPORTANTE! Esto es lo que mantiene la sesión activa durante esta
                // petición
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Mantenemos los atributos por si los usas en tus controllers
                request.setAttribute("userId", userId);
                request.setAttribute("rol", rol);
            }
        }

        // Continuar con el siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
}