package com.bacos.mokengeli.biloko.config;

import com.bacos.mokengeli.biloko.config.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers("/public/**").permitAll()

                        // Documentation API
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Actuator endpoints
                        .requestMatchers(
                                "/actuator/info",
                                "/actuator/health",
                                "/actuator/metrics/**",
                                "/actuator/websocket",
                                "/actuator/socketio"  // Nouvel endpoint Socket.io
                        ).permitAll()

                        // ✅ Socket.io endpoints - IMPORTANT
                        // Socket.io gère sa propre authentification au handshake
                        .requestMatchers(
                                "/socket.io/**",      // Socket.io path par défaut
                                "/socket.io/*"        // Socket.io variations
                        ).permitAll()

                        // ✅ WebSocket STOMP endpoints (temporaire pendant migration)
                        .requestMatchers(
                                "/api/order/ws/**",
                                "/api/order/ws/websocket/**",
                                "/api/order/ws/websocket"
                        ).permitAll()

                        // Test endpoints
                        .requestMatchers("/api/order/ws/test").permitAll()
                        .requestMatchers("/api/order/socketio/test").permitAll()
                        .requestMatchers("/public/ws-status").permitAll()

                        // Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}