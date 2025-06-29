package com.example.Project.filter;

import com.example.Project.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                Claims claims = jwtUtil.getAllClaimsFromToken(jwt);
                if (claims != null && !jwtUtil.isTokenExpired(jwt)) {
                    java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                    Object roleClaim = claims.get("role");
                    if (roleClaim != null) {
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + roleClaim.toString()));
                    }
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            claims.get("email"), null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Invalid token, do nothing, security context will not be set
            }
        }
        chain.doFilter(request, response);
    }
}
