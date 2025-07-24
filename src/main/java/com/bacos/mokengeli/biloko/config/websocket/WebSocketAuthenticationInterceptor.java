package com.bacos.mokengeli.biloko.config.websocket;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.cookie.access-token:accessToken}")
    private String cookieName;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        // ✅ LOGS DÉTAILLÉS pour debugging
        log.debug("🔌 WebSocket HANDSHAKE attempt detected!");
        log.debug("📍 Remote address: {}", request.getRemoteAddress());
        log.debug("🌐 URI: {}", request.getURI());
        log.debug("📋 Headers available: {}", request.getHeaders().keySet());

        // Log des headers importants pour debug
        log.debug("🔑 Authorization header: {}", request.getHeaders().getFirst("Authorization"));
        log.debug("🍪 Cookie header: {}", request.getHeaders().getFirst("Cookie"));
        log.debug("🏠 Origin header: {}", request.getHeaders().getFirst("Origin"));
        log.debug("👤 User-Agent: {}", request.getHeaders().getFirst("User-Agent"));

        try {
            // Extraire le token JWT
            String token = extractJwtToken(request);

            if (!StringUtils.hasText(token)) {
                log.error("❌ WebSocket handshake REJECTED - No JWT token found!");
                log.error("📋 All headers: {}", request.getHeaders());
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            log.debug("✅ JWT token found: {}...", token.substring(0, Math.min(20, token.length())));

            // Valider le token
            Claims claims = validateToken(token);
            if (claims == null) {
                log.error("❌ WebSocket handshake REJECTED - Invalid JWT token");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            log.debug("✅ JWT token validated successfully");
            log.debug("🎫 Claims: {}", claims);

            // Extraire les informations utilisateur
            String employeeNumber = claims.get("employeeNumber", String.class);
            String tenantCode = claims.get("tenantCode", String.class);
            String appType = claims.get("appType", String.class);

            if (!StringUtils.hasText(employeeNumber) || !StringUtils.hasText(tenantCode)) {
                log.error("❌ WebSocket handshake REJECTED - Missing user claims");
                log.error("👤 employeeNumber: {}, tenantCode: {}", employeeNumber, tenantCode);
                response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                return false;
            }

            // Stocker dans les attributs de session
            attributes.put("employeeNumber", employeeNumber);
            attributes.put("tenantCode", tenantCode);
            attributes.put("appType", appType);
            attributes.put("authenticated", true);

            log.debug("🎉 WebSocket handshake SUCCESSFUL!");
            log.debug("👤 User: {} | 🏢 Tenant: {} | 📱 App: {}", employeeNumber, tenantCode, appType);

            return true;

        } catch (Exception e) {
            log.error("💥 WebSocket handshake ERROR: {}", e.getMessage(), e);
            response.setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {

        if (exception != null) {
            log.error("💥 WebSocket handshake completed with ERROR: {}", exception.getMessage(), exception);
        } else {
            log.debug("✅ WebSocket handshake completed SUCCESSFULLY");
        }
    }

    private String extractJwtToken(ServerHttpRequest request) {
        log.debug("🔍 Searching for JWT token...");

        // 1. Query parameters (PRIORITÉ pour mobile)
        String query = request.getURI().getQuery();
        if (StringUtils.hasText(query) && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6);
                    log.debug("✅ Token found in query parameter");
                    return token;
                }
            }
        }

        // 2. Authorization header (web/API standard)
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                log.debug("✅ Token found in Authorization header");
                return authHeader.substring(7);
            }
        }

        // 3. Cookies (web classique)
        List<String> cookieHeaders = request.getHeaders().get("Cookie");
        if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
            for (String cookieHeader : cookieHeaders) {
                if (cookieHeader.contains(cookieName + "=")) {
                    String[] cookies = cookieHeader.split(";");
                    for (String cookie : cookies) {
                        cookie = cookie.trim();
                        if (cookie.startsWith(cookieName + "=")) {
                            log.debug("✅ Token found in cookie: {}", cookieName);
                            return cookie.substring((cookieName + "=").length());
                        }
                    }
                }
            }
        }

        log.warn("❌ No JWT token found in any location");
        return null;
    }
    private Claims validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.debug("✅ JWT validation successful");
            return claims;

        } catch (Exception e) {
            log.warn("❌ JWT validation failed: {}", e.getMessage());
            return null;
        }
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}