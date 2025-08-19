package com.bacos.mokengeli.biloko.config.websocket;

import com.bacos.mokengeli.biloko.config.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Intercepteur pour valider l'authentification sur les messages STOMP
 * Gère CONNECT, SUBSCRIBE, SEND
 */
@Slf4j
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        // Log des commandes STOMP pour debug
        if (command != null) {
            log.info("📨 STOMP Command: {} from session: {}",
                    command, accessor.getSessionId());
        }

        // Si pas de commande, laisser passer
        if (command == null) {
            return message;
        }

        // Traiter les différentes commandes STOMP
        switch (command) {
            case CONNECT:
                return handleConnect(message, accessor);
            case SUBSCRIBE:
                return handleSubscribe(message, accessor);
            case SEND:
                return handleSend(message, accessor);
            case DISCONNECT:
                return handleDisconnect(message, accessor);
            default:
                return message;
        }
    }

    /**
     * Gère la commande CONNECT
     * Valide le token et établit le contexte de sécurité
     */
    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        log.info("🔌 Processing STOMP CONNECT from session: {}", accessor.getSessionId());

        // Log des headers STOMP reçus
        log.debug("STOMP CONNECT headers: {}", accessor.toNativeHeaderMap());

        try {
            // Vérifier si l'authentification a déjà été faite au handshake
            Object authAttribute = accessor.getSessionAttributes().get("authenticated");
            if (Boolean.TRUE.equals(authAttribute)) {
                // Déjà authentifié lors du handshake HTTP
                String employeeNumber = (String) accessor.getSessionAttributes().get("employeeNumber");
                String tenantCode = (String) accessor.getSessionAttributes().get("tenantCode");

                // Récupérer le token stocké lors du handshake
                String token = (String) accessor.getSessionAttributes().get("token");

                // Créer l'authentification pour le contexte Spring Security
                if (StringUtils.hasText(token)) {
                    List<String> permissions = jwtService.getPermissions(token);
                    List<SimpleGrantedAuthority> authorities = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            employeeNumber, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    accessor.setUser(auth);
                }

                log.info("✅ STOMP CONNECT - Using authentication from handshake: {} (tenant: {})",
                        employeeNumber, tenantCode);
                return message;
            }

            // Si pas d'auth au handshake, essayer d'extraire le token des headers STOMP
            String token = extractTokenFromStompHeaders(accessor);

            if (!StringUtils.hasText(token)) {
                log.error("❌ No authentication found - neither from handshake nor STOMP headers");
                throw new SecurityException("Missing authentication");
            }

            // Valider le token
            if (!jwtService.validateToken(token)) {
                log.warn("❌ Invalid token in STOMP CONNECT");
                throw new SecurityException("Invalid authentication token");
            }

            // Extraire les informations utilisateur
            String employeeNumber = jwtService.extractUsername(token);
            String tenantCode = jwtService.getTenantCode(token);
            List<String> roles = jwtService.getRoles(token);
            List<String> permissions = jwtService.getPermissions(token);

            // Créer l'authentification
            List<SimpleGrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    employeeNumber, null, authorities);

            // Définir l'authentification dans le contexte
            SecurityContextHolder.getContext().setAuthentication(auth);
            accessor.setUser(auth);

            // Stocker les métadonnées dans les attributs de session
            accessor.getSessionAttributes().put("employeeNumber", employeeNumber);
            accessor.getSessionAttributes().put("tenantCode", tenantCode);
            accessor.getSessionAttributes().put("roles", roles);
            accessor.getSessionAttributes().put("authenticated", true);

            log.info("✅ STOMP CONNECT authenticated via headers: {} (tenant: {})",
                    employeeNumber, tenantCode);

        } catch (Exception e) {
            log.error("❌ STOMP CONNECT authentication failed: {}", e.getMessage());
            throw new SecurityException("Authentication failed: " + e.getMessage(), e);
        }

        return message;
    }

    /**
     * Gère la commande SUBSCRIBE
     * Vérifie que l'utilisateur peut s'abonner au topic demandé
     */
    private Message<?> handleSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();

        log.debug("📡 STOMP SUBSCRIBE to: {} (session: {})", destination, sessionId);

        // Récupérer le tenant de la session
        String tenantCode = (String) accessor.getSessionAttributes().get("tenantCode");

        if (tenantCode == null) {
            log.warn("❌ No tenant code in session for SUBSCRIBE");
            throw new SecurityException("No tenant code in session");
        }

        // Vérifier que l'utilisateur s'abonne uniquement à son tenant
        if (destination != null && destination.startsWith("/topic/orders/")) {
            String requestedTenant = destination.substring("/topic/orders/".length());
            if (!tenantCode.equals(requestedTenant)) {
                log.warn("❌ Unauthorized SUBSCRIBE: user tenant {} tried to subscribe to {}",
                        tenantCode, requestedTenant);
                throw new SecurityException("Cannot subscribe to other tenant's topics");
            }
        }

        log.info("✅ STOMP SUBSCRIBE authorized for: {}", destination);
        return message;
    }

    /**
     * Gère la commande SEND
     * Vérifie les permissions d'envoi
     */
    private Message<?> handleSend(Message<?> message, StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        log.debug("📤 STOMP SEND to: {}", destination);

        // Vérifier l'authentification
        if (accessor.getUser() == null) {
            log.warn("❌ Unauthenticated SEND attempt to: {}", destination);
            throw new SecurityException("Must be authenticated to send messages");
        }

        log.debug("✅ STOMP SEND authorized");
        return message;
    }

    /**
     * Gère la commande DISCONNECT
     */
    private Message<?> handleDisconnect(Message<?> message, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String employeeNumber = (String) accessor.getSessionAttributes().get("employeeNumber");

        // Log seulement si on a vraiment une session établie
        if (employeeNumber != null) {
            log.info("👋 STOMP DISCONNECT: {} (session: {})", employeeNumber, sessionId);
            // Nettoyer le contexte de sécurité
            SecurityContextHolder.clearContext();
        } else {
            log.debug("📤 STOMP DISCONNECT from unauthenticated session: {}", sessionId);
        }

        return message;
    }

    /**
     * Extrait le token des headers STOMP
     */
    private String extractTokenFromStompHeaders(StompHeaderAccessor accessor) {
        // 1. Header Authorization
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Header X-Auth-Token (custom)
        String customToken = accessor.getFirstNativeHeader("X-Auth-Token");
        if (StringUtils.hasText(customToken)) {
            return customToken;
        }

        // 3. Dans le login (pour compatibilité)
        String login = accessor.getLogin();
        if (StringUtils.hasText(login) && login.startsWith("Bearer ")) {
            return login.substring(7);
        }

        return null;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel,
                                    boolean sent, Exception ex) {
        if (!sent || ex != null) {
            log.error("❌ Message send failed: {}", ex != null ? ex.getMessage() : "Unknown error");
        }
    }
}