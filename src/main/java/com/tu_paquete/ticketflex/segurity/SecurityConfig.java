package com.tu_paquete.ticketflex.segurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final UserDetailsService userDetailsService;

        private final CustomLoginSuccessHandler successHandler;

        public SecurityConfig(UserDetailsService userDetailsService, CustomLoginSuccessHandler successHandler) {
                this.userDetailsService = userDetailsService;
                this.successHandler = successHandler;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF (solo para desarrollo)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/index", "/public/**", "/fonts/**", "/css/**",
                                                                "/js/**",
                                                                "/images/**", "/reset-password",
                                                                "/api/usuarios/reset-password-request",
                                                                "/api/usuarios/reset-password")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/admin/forgot-password",
                                                                "/admin/reset-password/**",
                                                                "/admin/reset-password-request")
                                                .permitAll()
                                                
                                                .requestMatchers("/admin/**").hasRole("Administrador")
                                                .requestMatchers("/login-superadmin", "/login-superadmin.html",
                                                                "superadmin-panel.html", "/politica-privacidad.html",
                                                                "/terminos-servicio.html")
                                                .permitAll()
                                                .requestMatchers("/api/eventos/listar").permitAll()
                                                .requestMatchers("/api/eventos/**").permitAll()
                                                .requestMatchers("/eventos/**").permitAll()
                                                .requestMatchers("/getimagen").permitAll()
                                                .requestMatchers("/api/usuarios/login").permitAll()
                                                .requestMatchers("/api/**").permitAll()
                                                .requestMatchers("/api/usuarios/auth/**").authenticated()
                                                .requestMatchers("/api/estadisticas/**").authenticated()
                                                .requestMatchers(
                                                                "/reset-password",
                                                                "/reset-password/**",
                                                                "/api/usuarios/reset-password-request",
                                                                "/api/usuarios/reset-password")
                                                .permitAll()

                                                .anyRequest().authenticated() // Todas las demás rutas requieren
                                                                              // autenticación
                                )
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler(successHandler) // Usa el handler personalizado
                                                .failureUrl("/login?error=true")
                                                .permitAll())

                                .httpBasic(httpBasic -> httpBasic
                                                .realmName("API Estadisticas"))

                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())
                                .exceptionHandling(exception -> exception
                                                .accessDeniedPage("/access-denied") // Nueva ruta para acceso denegado
                                );

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}