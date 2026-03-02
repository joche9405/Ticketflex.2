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
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * ¡ESTA ES LA CORRECCIÓN!
     * Indica qué rutas deben saltarse este filtro por completo.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Agregamos /api/imagen que es la ruta real de tu controlador
        return path.startsWith("/api/imagen") ||
                path.startsWith("/getimagen") ||
                path.startsWith("/api/eventos") ||
                path.startsWith("/eventos");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtUtil.validarToken(token)) {
                    String userId = jwtUtil.obtenerUserId(token);
                    String rol = jwtUtil.obtenerRol(token);

                    // Spring espera "ROLE_" para coincidir con hasRole en SecurityConfig
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.singletonList(authority));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    request.setAttribute("userId", userId);
                    request.setAttribute("rol", rol);
                }
            } catch (Exception e) {
                // Si el token es inválido o expiró, simplemente no autenticamos
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}