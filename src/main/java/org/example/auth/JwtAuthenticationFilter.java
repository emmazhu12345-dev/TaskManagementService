package org.example.auth;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.example.service.TokenBlocklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates Bearer tokens and sets SecurityContext. - Access tokens: used to authenticate and set
 * SecurityContext - Refresh tokens: allowed ONLY for /api/auth/refresh, never used to authenticate
 * Continues the chain regardless of token status (except explicit 401 cases).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlocklistService tokenBlocklistService;

    public JwtAuthenticationFilter(
            JwtService jwtService, UserDetailsService userDetailsService, TokenBlocklistService tokenBlocklistService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("JWT filter path={}", path);

        // If already authenticated, continue
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 1) Blocklist check for ALL tokens (access & refresh)
        boolean blocked = false;
        try {
            blocked = tokenBlocklistService.isBlocked(token);
        } catch (Exception e) {
            log.error("Failed to check token blocklist from Redis", e);
            // 这里有两种策略：
            // 1) 保守：当 Redis 出问题时，一律视为 blocked，拒绝请求（更安全）
            // 2) 放宽：当 Redis 出问题时，当作不在 blocklist，允许请求（更可用）
        }
        if (blocked) {
            log.debug("Token is in blocklist");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been invalidated. Please log in again.");
            return;
        }

        try {
            // 2) Distinguish access vs refresh
            boolean isAccessToken = jwtService.isTokenType(token, "access");
            boolean isRefreshToken = jwtService.isTokenType(token, "refresh");

            // For refresh tokens:
            if (isRefreshToken) {
                // Only allowed on the refresh endpoint; never used to set authentication
                if (path.startsWith("/api/auth/refresh")) {
                    log.debug("Refresh token used on /api/auth/refresh, skipping authentication setup");
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    log.debug("Refresh token used on non-refresh endpoint, rejecting");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Refresh token cannot be used to access this resource.");
                    return;
                }
            }

            // If it's not an access token (unknown typ), just continue without authentication
            if (!isAccessToken) {
                log.debug("Token type is not access/refresh, skipping authentication");
                filterChain.doFilter(request, response);
                return;
            }

            // 3) Access token flow: validate & set SecurityContext
            String username = jwtService.parseUsername(token);
            log.debug("Parsed username={}", username);

            if (username != null && !username.isBlank()) {
                var userDetails = userDetailsService.loadUserByUsername(username);

                // Optional：Can check if the token is expired, reject the request fast
                // if (jwtService.isTokenExpired(token)) { ... }

                var authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authentication set for {}", username);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // Invalid or expired token: ignore and continue (no auth set)
            log.debug("JWT invalid: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
