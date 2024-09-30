package com.bacos.mokengeli.biloko.config.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {


    private final String secretKey;

    @Autowired
    public JwtService(@Value("${security.jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token) {
        final String username = extractUsername(token);
        return (username != null && !isTokenExpired(token));
    }


    private Key getSignKey() {
        // byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }


    private static String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
        Set<String> authoritiesSet = new HashSet<>();
        for (GrantedAuthority authority : collection) {
            authoritiesSet.add(authority.getAuthority());
        }
        return String.join(",", authoritiesSet);
    }


    public List<GrantedAuthority> getAuthoritiesFromJWT(String token) {
        Claims claims = extractAllClaims(token);
        List<String> authorities = claims.get("roles", List.class);
        return authorities.stream().map(x -> new SimpleGrantedAuthority(x)).collect(Collectors.toList());
    }

    public String getTenantCode(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("tenantCode", String.class);
    }

    public List<String> getRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public List<String> getPermissions(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("permissions", List.class);
    }
}
