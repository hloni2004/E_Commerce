
package za.ac.styling.security;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret:change_this_in_prod}")
    private String jwtSecret;

    @Value("${security.jwt.expirationMillis:900000}") // default 15 minutes
    private long jwtExpirationMillis;

    @Value("${security.jwt.refreshExpirationMillis:604800000}") // default 7 days
    private long refreshExpirationMillis;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Overload to support custom claims (e.g., roles)
    public String generateAccessToken(String subject, java.util.Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAccessToken(String subject) {
        return generateAccessToken(subject, new java.util.HashMap<>());
    }

    public String generateRefreshToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpirationMillis);
        return Jwts.builder().setSubject(subject).setIssuedAt(now).setExpiration(exp).signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
    }

    // Extract roles from JWT claims (assumes roles are stored as a claim named
    // "roles" as a comma-separated string or list)
    public List<String> getRoles(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof String rolesStr) {
            return List.of(rolesStr.split(","));
        } else if (rolesObj instanceof Collection rolesCol) {
            return (List<String>) rolesCol.stream().map(Object::toString).collect(Collectors.toList());
        }
        return List.of();
    }
}