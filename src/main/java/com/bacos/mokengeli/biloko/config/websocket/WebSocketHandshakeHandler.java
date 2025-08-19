package com.bacos.mokengeli.biloko.config.websocket;

import com.bacos.mokengeli.biloko.config.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire de handshake WebSocket natif
 * Extrait et valide le JWT pendant la phase de handshake HTTP
 */
@Slf4j
@Component
public class WebSocketHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtService jwtService;

    public WebSocketHandshakeHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        log.info("🤝 WebSocket handshake authentication starting...");
        log.info("📍 URI: {}", request.getURI());
        log.info("📋 Headers present: {}", request.getHeaders().keySet());

        // Log des headers WebSocket importants
        log.debug("Upgrade: {}", request.getHeaders().getFirst("Upgrade"));
        log.debug("Connection: {}", request.getHeaders().getFirst("Connection"));
        log.debug("Sec-WebSocket-Protocol: {}", request.getHeaders().getFirst("Sec-WebSocket-Protocol"));

        try {
            // Extraire le token JWT
            String token = extractJwtToken(request);

            if (!StringUtils.hasText(token)) {
                log.error("❌ No JWT token found in handshake request");
                log.error("Query params: {}", request.getURI().getQuery());
                log.error("Authorization header: {}", request.getHeaders().getFirst("Authorization"));
                log.error("Cookie header: {}", request.getHeaders().getFirst("Cookie"));
                return null;
            }

            log.info("🎫 JWT token found, validating...");

            // Valider le token
            if (!jwtService.validateToken(token)) {
                log.error("❌ Invalid JWT token");
                return null;
            }

            // Extraire les informations du token
            String employeeNumber = jwtService.extractUsername(token);
            String tenantCode = jwtService.getTenantCode(token);
            List<String> roles = jwtService.getRoles(token);

            // Stocker les infos dans les attributs de session
            attributes.put("employeeNumber", employeeNumber);
            attributes.put("tenantCode", tenantCode);
            attributes.put("roles", roles);
            attributes.put("authenticated", true);
            attributes.put("token", token); // Stocker le token pour usage ultérieur

            log.info("✅ WebSocket authentication successful for user: {} (tenant: {})",
                    employeeNumber, tenantCode);

            // Créer un Principal pour identifier l'utilisateur
            return new WebSocketUserPrincipal(employeeNumber, tenantCode);

        } catch (Exception e) {
            log.error("💥 Error during WebSocket authentication: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extrait le token JWT de la requête
     * Priorité : Query param > Authorization header > Cookie
     */
    private String extractJwtToken(ServerHttpRequest request) {
        // 1. Query parameter (priorité pour React Native)
        String token = extractTokenFromQuery(request);
        if (StringUtils.hasText(token)) {
            log.debug("✅ Token found in query parameter");
            return token;
        }

        // 2. Authorization header
        token = extractTokenFromHeader(request);
        if (StringUtils.hasText(token)) {
            log.debug("✅ Token found in Authorization header");
            return token;
        }

        // 3. Cookie (pour web)
        token = extractTokenFromCookie(request);
        if (StringUtils.hasText(token)) {
            log.debug("✅ Token found in cookie");
            return token;
        }

        log.debug("❌ No token found in any location");
        return null;
    }

    private String extractTokenFromQuery(ServerHttpRequest request) {
        try {
            Map<String, List<String>> params = UriComponentsBuilder
                    .fromUri(request.getURI())
                    .build()
                    .getQueryParams();

            List<String> tokenParams = params.get("token");
            if (tokenParams != null && !tokenParams.isEmpty()) {
                return tokenParams.get(0);
            }
        } catch (Exception e) {
            log.error("Error extracting token from query: {}", e.getMessage());
        }
        return null;
    }

    private String extractTokenFromHeader(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    private String extractTokenFromCookie(ServerHttpRequest request) {
        List<String> cookieHeaders = request.getHeaders().get("Cookie");
        if (cookieHeaders != null) {
            for (String cookieHeader : cookieHeaders) {
                String[] cookies = cookieHeader.split(";");
                for (String cookie : cookies) {
                    cookie = cookie.trim();
                    if (cookie.startsWith("accessToken=")) {
                        return cookie.substring("accessToken=".length());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Principal personnalisé pour identifier l'utilisateur WebSocket
     */
    public static class WebSocketUserPrincipal implements Principal {
        private final String employeeNumber;
        private final String tenantCode;

        public WebSocketUserPrincipal(String employeeNumber, String tenantCode) {
            this.employeeNumber = employeeNumber;
            this.tenantCode = tenantCode;
        }

        @Override
        public String getName() {
            return employeeNumber;
        }

        public String getTenantCode() {
            return tenantCode;
        }

        @Override
        public String toString() {
            return "WebSocketUser[" + employeeNumber + "@" + tenantCode + "]";
        }
    }
}