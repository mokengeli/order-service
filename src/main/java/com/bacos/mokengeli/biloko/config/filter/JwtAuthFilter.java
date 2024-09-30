package com.bacos.mokengeli.biloko.config.filter;

import com.bacos.mokengeli.biloko.application.domain.model.ConnectedUser;
import com.bacos.mokengeli.biloko.config.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Autowired
    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String username = null;

        // Récupérer le token JWT à partir des cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                    username = jwtService.extractUsername(token);
                }
            }
        }

        // Si aucun token n'est trouvé, passer au filtre suivant
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Si l'utilisateur n'est pas encore authentifié, authentifier à partir du token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.validateToken(token)) {
                String tenantCode = this.jwtService.getTenantCode(token);
                List<String> roles = this.jwtService.getRoles(token);
                List<String> permissions = this.jwtService.getPermissions(token);
                List<GrantedAuthority> grantedAuthorities = getAuthoritiesFromJWT(permissions);

                ConnectedUser connectedUser = createUser(username, tenantCode, roles, permissions);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(connectedUser, null, grantedAuthorities);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private ConnectedUser createUser(String username, String tenantCode, List<String> roles, List<String> permissions) {

        return ConnectedUser.builder()
                .employeeNumber(username)
                .tenantCode(tenantCode)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    public List<GrantedAuthority> getAuthoritiesFromJWT(List<String> authorities) {
        return authorities.stream().map(x -> new SimpleGrantedAuthority(x)).collect(Collectors.toList());
    }
}
