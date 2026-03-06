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
                path.startsWith("/favicon.ico") ||
                path.startsWith("/api/superadmin/login-superadmin") ||
                path.startsWith("/login-superadmin.html") ||
                path.startsWith("/images/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        // LOG 1: Entrada de la petición
        System.out.println(">>> [JWT FILTER] Petición recibida en: " + path + " [" + method + "]");

        String token = null;

        // 1. Intentar obtener el token del Header Authorization
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            System.out.println(">>> [JWT FILTER] Token encontrado en Header Authorization");
        }
        // 2. Si no hay header, buscar en las Cookies
        else if (request.getCookies() != null) {
            System.out.println(">>> [JWT FILTER] Buscando en cookies... Total: " + request.getCookies().length);
            for (Cookie cookie : request.getCookies()) {
                System.out.println(">>> [JWT FILTER] Cookie vista: " + cookie.getName());
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println(">>> [JWT FILTER] Cookie 'token' detectada con éxito");
                    break;
                }
            }
        } else {
            System.out.println(">>> [JWT FILTER] No se detectaron cookies ni Header en la petición");
        }

        // 3. Validar el token y autenticar
        if (token != null) {
            try {
                if (jwtUtil.validarToken(token)) {
                    String userId = jwtUtil.obtenerUserId(token);
                    String rol = jwtUtil.obtenerRol(token).trim();

                    System.out.println(">>> [JWT FILTER] Token VÁLIDO. Usuario: " + userId + " | Rol: " + rol);

                    // --- CORRECCIÓN: Doble autoridad para evitar fallos de matching ---
                    List<SimpleGrantedAuthority> authorities = Arrays.asList(
                            new SimpleGrantedAuthority("ROLE_" + rol),
                            new SimpleGrantedAuthority(rol));

                    System.out.println(">>> [JWT FILTER] Autoridades asignadas: " + authorities);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println(">>> [JWT FILTER] Seguridad establecida en el contexto de Spring");

                    request.setAttribute("userId", userId);
                    request.setAttribute("rol", rol);
                } else {
                    System.out.println(
                            ">>> [JWT FILTER] ALERTA: El token existe pero NO es válido (expirado o malformado)");
                }
            } catch (Exception e) {
                System.out.println(">>> [JWT FILTER] ERROR procesando token: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            System.out.println(">>> [JWT FILTER] No se pudo extraer ningún token. La petición sigue como ANÓNIMA");
        }

        filterChain.doFilter(request, response);
    }
}