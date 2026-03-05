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
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

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
                                        // CORRECCIÓN: Si usas allowCredentials(true), NO puedes usar "*" en Origins
                                        opt.setAllowedOriginPatterns(List.of("*"));
                                        opt.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                                        opt.setAllowedHeaders(List.of("*"));
                                        opt.setAllowCredentials(true);
                                        return opt;
                                }))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // 1. RECURSOS PÚBLICOS
                                                .requestMatchers("/", "/index", "/index.html", "/login", "/login.html",
                                                                "/registro", "/registro.html", "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/public/**", "/fonts/**", "/css/**", "/js/**",
                                                                "/images/**")
                                                .permitAll()
                                                .requestMatchers("/politica-privacidad.html", "/terminos-servicio.html",
                                                                "/como-comprar.html")
                                                .permitAll()
                                                .requestMatchers("/api/imagen/**", "/api/eventos/**", "/eventos/**",
                                                                "/getimagen/**")
                                                .permitAll()
                                                .requestMatchers("/api/usuarios/login", "/api/usuarios/registrar",
                                                                "/api/usuarios/logout")
                                                .permitAll()
                                                .requestMatchers("/api/usuarios/reset-password**",
                                                                "/admin/reset-password/**")
                                                .permitAll()

                                                // 2. RUTAS DE ADMINISTRADOR (Ajustadas)
                                                .requestMatchers("/admin/**")
                                                .hasAnyAuthority("ROLE_Administrador", "Administrador", "ROLE_ADMIN",
                                                                "ADMIN")

                                                // 3. RESTO AUTENTICADO
                                                .requestMatchers("/api/usuarios/auth/**", "/api/estadisticas/**")
                                                .authenticated()
                                                .anyRequest().authenticated())

                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(exception -> exception
                                                .accessDeniedPage("/access-denied"));

                return http.build();
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring().requestMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**",
                                "/static/**", "/resources/**");
        }
}