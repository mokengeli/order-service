package com.bacos.mokengeli.biloko.config.socketio;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Slf4j
@Component
public class SocketIOHandshakeInterceptor implements ConnectListener, DisconnectListener {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.cookie.access-token:accessToken}")
    private String cookieName;

    @Override
    public void onConnect(SocketIOClient client) {
        HandshakeData data = client.getHandshakeData();
        String token = extractJwtToken(data);
        if (!StringUtils.hasText(token)) {
            log.error("❌ Socket.IO connection rejected - No JWT token");
            client.disconnect();
            return;
        }

        Claims claims = validateToken(token);
        if (claims == null) {
            log.error("❌ Socket.IO connection rejected - Invalid JWT token");
            client.disconnect();
            return;
        }

        String employeeNumber = claims.get("employeeNumber", String.class);
        String tenantCode = claims.get("tenantCode", String.class);
        String appType = claims.get("appType", String.class);

        if (!StringUtils.hasText(employeeNumber) || !StringUtils.hasText(tenantCode)) {
            log.error("❌ Socket.IO connection rejected - Missing user claims");
            client.disconnect();
            return;
        }

        client.set("employeeNumber", employeeNumber);
        client.set("tenantCode", tenantCode);
        client.set("appType", appType);
        client.set("authenticated", true);

        log.debug("✅ Socket.IO handshake successful for user {} tenant {}", employeeNumber, tenantCode);
    }

    @Override
    public void onDisconnect(SocketIOClient client) {
        log.debug("🔌 Socket.IO client disconnected {}", client.getSessionId());
    }

    private String extractJwtToken(HandshakeData data) {
        String token = data.getSingleUrlParam("token");
        if (StringUtils.hasText(token)) {
            return token;
        }

        String authHeader = data.getHttpHeaders().get("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        String cookieHeader = data.getHttpHeaders().get("Cookie");
        if (StringUtils.hasText(cookieHeader)) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.startsWith(cookieName + "=")) {
                    return cookie.substring((cookieName + "=").length());
                }
            }
        }

        return null;
    }

    private Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("❌ JWT validation failed: {}", e.getMessage());
            return null;
        }
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
