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

import org.springframework.lang.NonNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
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
                path.startsWith("/api/pagos/confirmacion-payu") ||
                path.startsWith("/favicon.ico") ||
                path.equals("/superadmin/login") || // La ruta que definimos para el
                path.equals("/api/superadmin/login-superadmin") ||
                path.contains("login-superadmin") ||
                path.startsWith("/api/superadmin/login-superadmin") ||
                path.startsWith("/login-superadmin.html") ||
                path.equals("/superadmin/login") ||
                path.equals("/api/superadmin/login-superadmin") ||
                path.startsWith("/images/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        System.out.println(">>> [JWT FILTER] Petición recibida en: " + path + " [" + method + "]");

        String token = null;

        // 1. Intentar obtener el token del Header Authorization
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        // 2. Buscar en Cookies si no hay header
        else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3. Validar el token y autenticar
        if (token != null) {
            try {
                if (jwtUtil.validarToken(token)) {
                    // EXTRACCIÓN DE DATOS
                    String userId = jwtUtil.obtenerUserId(token);
                    String rol = jwtUtil.obtenerRol(token).trim();

                    // --- CAMBIO CLAVE: Obtener el EMAIL para el PagoController ---
                    // Si tu obtenerUserId ya devuelve el email, estamos bien.
                    // Si devuelve el ID numérico/hex, necesitas un método obtenerEmail en jwtUtil.
                    String email = jwtUtil.obtenerUserId(token); // O jwtUtil.obtenerEmail(token);

                    System.out.println(">>> [JWT FILTER] Autenticando email: " + email);

                    List<SimpleGrantedAuthority> authorities = Arrays.asList(
                            new SimpleGrantedAuthority("ROLE_" + rol),
                            new SimpleGrantedAuthority(rol));

                    // Guardamos el EMAIL como el "Name" de la autenticación
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Guardamos atributos adicionales para uso interno en el Request
                    request.setAttribute("userId", userId);
                    request.setAttribute("userEmail", email);
                    request.setAttribute("rol", rol);

                } else {
                    System.out.println(">>> [JWT FILTER] Token inválido");
                }
            } catch (Exception e) {
                System.out.println(">>> [JWT FILTER] ERROR: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}