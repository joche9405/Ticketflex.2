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
                                        opt.setAllowedOriginPatterns(List.of("*"));
                                        opt.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                                        opt.setAllowedHeaders(List.of("*"));
                                        opt.setExposedHeaders(List.of("Authorization")); // Exponer el header
                                        opt.setAllowCredentials(true);
                                        return opt;
                                }))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Prioridad máxima a las peticiones OPTIONS y al Login de SuperAdmin
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                                .permitAll()
                                                .requestMatchers("/superadmin/login",
                                                                "/api/superadmin/login-superadmin")
                                                .permitAll() // filtros

                                                // Recursos públicos
                                                .requestMatchers("/", "/index", "/index.html", "/login", "/login.html",
                                                                "/registro", "/registro.html", "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/public/**", "/css/**", "/js/**", "/images/**",
                                                                "/fonts/**")
                                                .permitAll()
                                                .requestMatchers("/api/imagen/**", "/api/eventos/**", "/eventos/**",
                                                                "/getimagen/**")
                                                .permitAll()
                                                .requestMatchers("/api/usuarios/login", "/api/usuarios/registrar",
                                                                "/api/usuarios/logout")
                                                .permitAll()

                                                // Restricciones por Rol (Asegúrate de que el rol en DB sea
                                                // "SuperAdmin")
                                                .requestMatchers("/api/superadmin/**")
                                                .hasAnyAuthority("ROLE_SuperAdmin", "SuperAdmin")

                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring().requestMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**",
                                "/static/**", "/resources/**");
        }
}