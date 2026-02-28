package com.tu_paquete.ticketflex.segurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                                // 1. STATELESS: No guardamos sesiones en el servidor
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/index", "/public/**", "/fonts/**", "/css/**",
                                                                "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/politica-privacidad.html", "/terminos-servicio.html",
                                                                "/como-comprar.html")
                                                .permitAll()
                                                .requestMatchers("/api/usuarios/login", "/api/usuarios/registrar",
                                                                "/api/usuarios/reset-password**",
                                                                "/admin/reset-password/**")
                                                .permitAll()
                                                .requestMatchers("/api/eventos/**", "/eventos/**", "/getimagen/**")
                                                .permitAll()
                                                .requestMatchers("/admin/**").hasRole("Administrador")
                                                .requestMatchers("/api/usuarios/auth/**").authenticated()
                                                .requestMatchers("/api/estadisticas/**").authenticated()
                                                .anyRequest().authenticated())

                                // 2. Filtro JWT: El corazón de la nueva autenticación
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                                .exceptionHandling(exception -> exception
                                                .accessDeniedPage("/access-denied"));

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}