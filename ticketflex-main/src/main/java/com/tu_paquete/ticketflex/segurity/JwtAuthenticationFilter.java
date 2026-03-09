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
        // Mantenemos TODA tu lista original para asegurar compatibilidad
        return path.startsWith("/api/usuarios/login") ||
                path.startsWith("/api/usuarios/registrar") ||
                path.startsWith("/api/imagen") ||
                path.startsWith("/getimagen") ||
                path.startsWith("/api/eventos") || // Esto libera /api/eventos/listar
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
                path.equals("/superadmin/login") ||
                path.equals("/api/superadmin/login-superadmin") ||
                path.contains("login-superadmin") ||
                path.startsWith("/login-superadmin.html") ||
                path.startsWith("/images/") ||
                path.startsWith("/fonts/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        // Log útil para depuración en Render
        System.out.println(">>> [JWT FILTER] Procesando: " + path + " [" + method + "]");

        String token = null;

        // 1. Intentar obtener el token del Header Authorization
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        // 2. Si no hay header, buscar en las Cookies
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
                    // Extraemos los datos del token
                    String userId = jwtUtil.obtenerUserId(token);
                    String rol = jwtUtil.obtenerRol(token).trim();

                    // Importante: Usamos el email/userId como el 'Principal' para PagoController
                    String email = userId;

                    System.out.println(">>> [JWT FILTER] Usuario autenticado: " + email);

                    // Doble autoridad para evitar fallos de matching en SecurityConfig
                    List<SimpleGrantedAuthority> authorities = Arrays.asList(
                            new SimpleGrantedAuthority("ROLE_" + rol),
                            new SimpleGrantedAuthority(rol));

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            email, null, authorities);

                    // Establecemos la autenticación en el contexto de Spring
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Atributos útiles para el objeto Request
                    request.setAttribute("userId", userId);
                    request.setAttribute("userEmail", email);
                    request.setAttribute("rol", rol);

                } else {
                    System.out.println(">>> [JWT FILTER] Token inválido o expirado");
                }
            } catch (Exception e) {
                System.out.println(">>> [JWT FILTER] ERROR procesando token: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}