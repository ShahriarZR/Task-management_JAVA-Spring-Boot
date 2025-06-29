package com.example.Project.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "015bcb872aa134e63e5e97c1fcb3fda1444a0c1b52cc9d8c16d22cdfd200b787456326eb51b00031189a77e831f22a3fbcbf982c05ef9d880b7b7fa518ca4f19"; // Replace with a secure key

    private final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5 hours in milliseconds

    public String generateToken(Long id, String name, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("name", name);
        claims.put("email", email);
        claims.put("role", role);
        return doGenerateToken(claims);
    }

    private String doGenerateToken(Map<String, Object> claims) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + JWT_TOKEN_VALIDITY);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getAllClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }
}
