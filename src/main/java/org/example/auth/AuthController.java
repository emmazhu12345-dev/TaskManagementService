package org.example.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.example.service.TokenBlocklistService;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(String username, String email, String password) {}
    public record LoginRequest(String username, String password) {}

    // login & refresh response,：access + refresh
    public record TokenResponse(String token, String refreshToken) {}

    // refresh request body（optional, can use header）
    public record RefreshRequest(String refreshToken) {}

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlocklistService tokenBlocklistService;

    public AuthController(AuthenticationManager authManager,
                          JwtService jwtService,
                          UserService userService,
                          TokenBlocklistService tokenBlocklistService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest req) {
        // TODO: validate input, check duplicate username/email, etc.
        userService.register(req.username(), req.email(), req.password());
        return ResponseEntity.ok().build();
    }

    /**
     * Login: return access token + refresh token
     */
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        var auth = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        authManager.authenticate(auth);

        String accessToken = jwtService.generateAccessToken(req.username());
        String refreshToken = jwtService.generateRefreshToken(req.username());

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * Logout: put current token into blocklist
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Try to extract exp claim from token
            Long expMillis = jwtService.extractExpirationMillis(token);
            if (expMillis == null) {
                expMillis = Instant.now().plusSeconds(24 * 3600).toEpochMilli();
            }
            tokenBlocklistService.blockToken(token, expMillis);
        }

        return ResponseEntity.ok(Map.of("message", "Logout successful."));
    }

    /**
     * Refresh: use refresh token to get a new access token + refresh token
     * - Accept token from Authorization header OR request body
     * - Rotate refresh token: old one is added to blocklist
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     @RequestBody(required = false) RefreshRequest body) {
        String token = extractTokenFromHeaderOrBody(request, body);

        if (token == null || token.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Missing refresh token"));
        }

        // 1) Must be a refresh token
        if (!jwtService.isTokenType(token, "refresh")) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Invalid token type. Expecting refresh token."));
        }

        // 2) Cannot be blocklisted
        if (tokenBlocklistService.isBlocked(token)) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Refresh token has been invalidated."));
        }

        // 3) Cannot be expired
        if (jwtService.isTokenExpired(token)) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Refresh token has expired."));
        }

        // 4) Load username from token and generate new tokens
        String username = jwtService.parseUsername(token);
        String newAccessToken = jwtService.generateAccessToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        // 5) Rotate refresh token: block the old one
        Long expMillis = jwtService.extractExpirationMillis(token);
        if (expMillis == null) {
            expMillis = Instant.now().plusSeconds(24 * 3600).toEpochMilli();
        }
        tokenBlocklistService.blockToken(token, expMillis);

        return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken));
    }

    /**
     * Helper: get refresh token either from Authorization header or request body
     */
    private String extractTokenFromHeaderOrBody(HttpServletRequest request, RefreshRequest body) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return body.refreshToken();
        }
        return null;
    }
}
