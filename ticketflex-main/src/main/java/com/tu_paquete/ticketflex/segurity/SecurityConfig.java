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
                                        // En producción, es mejor listar los orígenes exactos (Render y Local)
                                        opt.setAllowedOriginPatterns(List.of("*"));
                                        opt.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                                        opt.setAllowedHeaders(List.of("*"));
                                        opt.setExposedHeaders(List.of("Authorization"));
                                        opt.setAllowCredentials(true);
                                        return opt;
                                }))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                                .permitAll()

                                                // --- RUTAS PÚBLICAS DE PAGO ---
                                                .requestMatchers("/api/pagos/confirmacion-payu").permitAll()

                                                // Acceso a login y registro
                                                .requestMatchers("/superadmin/login",
                                                                "/api/superadmin/login-superadmin")
                                                .permitAll()
                                                .requestMatchers("/api/usuarios/login", "/api/usuarios/registrar",
                                                                "/api/usuarios/logout")
                                                .permitAll()

                                                // Recursos estáticos y vistas
                                                .requestMatchers("/", "/index", "/index.html", "/login", "/login.html",
                                                                "/registro", "/registro.html", "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/public/**", "/css/**", "/js/**", "/images/**",
                                                                "/fonts/**")
                                                .permitAll()

                                                // APIs de consulta pública
                                                .requestMatchers("/api/imagen/**", "/api/eventos/**", "/eventos/**",
                                                                "/getimagen/**")
                                                .permitAll()

                                                // Restricciones de administración
                                                .requestMatchers("/api/superadmin/**")
                                                .hasAnyAuthority("ROLE_SuperAdmin", "SuperAdmin")

                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                // Ignorar recursos estáticos para que no pasen por ningún filtro de seguridad
                return (web) -> web.ignoring().requestMatchers(
                                "/favicon.ico", "/css/**", "/js/**", "/images/**", "/static/**", "/resources/**",
                                "/fonts/**");
        }
}