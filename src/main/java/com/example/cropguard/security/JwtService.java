package com.example.cropguard.security;

import com.example.cropguard.config.AppProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(AppProperties props) {
        this.key = Keys.hmacShaKeyFor(props.jwtSecret().getBytes());
        this.expirationSeconds = props.jwtExpirationSeconds();
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000);
        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().get("role", String.class);
    }
}
