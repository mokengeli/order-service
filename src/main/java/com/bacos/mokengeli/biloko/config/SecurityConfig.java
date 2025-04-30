package com.bacos.mokengeli.biloko.config;

import com.bacos.mokengeli.biloko.config.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    @Autowired
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(
                csrf -> csrf.disable()
                )  // Désactivation de la protection CSRF, car on utilise JWT
                //.cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/public/**").permitAll()  // Routes publiques
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/info", "/actuator/health").permitAll()
                        .anyRequest().authenticated()  // Toutes les autres routes nécessitent une authentification
                )
                //.cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS configuration
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Pas de sessions, JWT uniquement
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)  // Ajouter le filtre JWT
                .build();
    }


}
