package com.tu_paquete.ticketflex.segurity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        // Retornamos true para las rutas que NO deben ser filtradas (públicas)
        return path.startsWith("/api/usuarios/login") ||
                path.startsWith("/api/usuarios/registrar") ||
                path.startsWith("/api/imagen") ||
                path.startsWith("/getimagen") ||
                path.startsWith("/api/eventos") ||
                path.startsWith("/eventos") ||
                path.startsWith("/admin/forgot-password") ||
                path.startsWith("/admin/reset-password") ||
                path.startsWith("/public") ||
                path.startsWith("/login") ||
                path.equals("/login.html") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // 1. Intentar obtener el token del Header Authorization
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        // 2. Si no hay header, buscar en las Cookies (Fundamental para el Dashboard
        // Admin)
        else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3. Si hay un token, validarlo y autenticar
        if (token != null) {
            try {
                if (jwtUtil.validarToken(token)) {
                    String userId = jwtUtil.obtenerUserId(token);
                    String rol = jwtUtil.obtenerRol(token);

                    // Spring espera "ROLE_" para coincidir con .hasRole() en SecurityConfig
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.singletonList(authority));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Guardamos info extra en el request por si la necesitamos en los controladores
                    request.setAttribute("userId", userId);
                    request.setAttribute("rol", rol);
                }
            } catch (Exception e) {
                // En caso de error (token expirado o manipulado), limpiamos el contexto
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}