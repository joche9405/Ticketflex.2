package com.tu_paquete.ticketflex.segurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtFilter;

        // Solo inyectamos lo que realmente usamos: el filtro de JWT
        public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
                this.jwtFilter = jwtFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(request -> {
                                        var opt = new org.springframework.web.cors.CorsConfiguration();
                                        opt.setAllowedOrigins(java.util.List.of("*"));
                                        opt.setAllowedMethods(
                                                        java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                                        opt.setAllowedHeaders(java.util.List.of("*"));
                                        return opt;
                                }))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // 1. RECURSOS ESTÁTICOS Y PÚBLICOS (Prioridad alta)
                                                .requestMatchers("/", "/index", "/public/**", "/fonts/**", "/css/**",
                                                                "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/politica-privacidad.html", "/terminos-servicio.html",
                                                                "/como-comprar.html")
                                                .permitAll()

                                                // 2. ENDPOINTS DE IMÁGENES Y EVENTOS (Públicos)
                                                // Asegúrate de incluir tanto la ruta con /api como la directa si ambas
                                                // existen
                                                .requestMatchers("/api/imagen/**", "/api/eventos/**", "/eventos/**",
                                                                "/getimagen/**")
                                                .permitAll()

                                                // 3. AUTH (Login y Registro)
                                                .requestMatchers("/api/usuarios/login", "/api/usuarios/registrar",
                                                                "/api/usuarios/reset-password**", "/login.html",
                                                                "/admin/reset-password/**")
                                                .permitAll()

                                                // 4. RUTAS PROTEGIDAS
                                                .requestMatchers("/admin/**").hasRole("Administrador")
                                                .requestMatchers("/api/usuarios/auth/**", "/api/estadisticas/**")
                                                .authenticated()

                                                .anyRequest().authenticated())

                                // 5. FILTRO JWT
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                                .exceptionHandling(exception -> exception
                                                .accessDeniedPage("/access-denied"));

                return http.build();
        }
}