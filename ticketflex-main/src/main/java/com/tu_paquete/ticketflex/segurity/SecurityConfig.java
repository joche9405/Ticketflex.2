package com.tu_paquete.ticketflex.segurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
                this.jwtFilter = jwtFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(request -> {
                                        CorsConfiguration opt = new CorsConfiguration();
                                        opt.setAllowedOrigins(List.of("*")); // En producción, es mejor especificar el
                                                                             // dominio
                                        opt.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                                        opt.setAllowedHeaders(List.of("*"));
                                        opt.setAllowCredentials(true); // Importante para permitir el envío de Cookies
                                        return opt;
                                }))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // 1. RECURSOS ESTÁTICOS Y PÁGINAS BASE
                                                .requestMatchers("/", "/index", "/index.html", "/login", "/login.html",
                                                                "/registro", "/registro.html")
                                                .permitAll()
                                                .requestMatchers("/public/**", "/fonts/**", "/css/**", "/js/**",
                                                                "/images/**")
                                                .permitAll()
                                                .requestMatchers("/politica-privacidad.html", "/terminos-servicio.html",
                                                                "/como-comprar.html")
                                                .permitAll()

                                                // 2. ENDPOINTS PÚBLICOS DE API (Imágenes y Eventos)
                                                .requestMatchers("/api/imagen/**", "/api/eventos/**", "/eventos/**",
                                                                "/getimagen/**")
                                                .permitAll()

                                                // 3. AUTH (Login, Registro y Reset)
                                                // Agregamos el logout a la lista de permitidos para que el JS pueda
                                                // limpiarlo siempre
                                                .requestMatchers("/api/usuarios/login", "/api/usuarios/registrar",
                                                                "/api/usuarios/logout")
                                                .permitAll()
                                                .requestMatchers("/api/usuarios/reset-password**",
                                                                "/admin/reset-password/**")
                                                .permitAll()

                                                // 4. RUTAS PROTEGIDAS PARA ADMINISTRADORES
                                                // hasRole("Administrador") buscará "ROLE_Administrador" en el filtro
                                                .requestMatchers("/admin/**").hasRole("Administrador")

                                                // 5. RUTAS PARA USUARIOS AUTENTICADOS EN GENERAL
                                                .requestMatchers("/api/usuarios/auth/**", "/api/estadisticas/**")
                                                .authenticated()

                                                .anyRequest().authenticated())

                                // 6. FILTRO JWT (Antes del filtro de autenticación por defecto)
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                                .exceptionHandling(exception -> exception
                                                .accessDeniedPage("/access-denied"));

                return http.build();
        }
}