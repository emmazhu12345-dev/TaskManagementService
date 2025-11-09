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
    public record TokenResponse(String token) {}

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenBlocklistService tokenBlocklistService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UserService userService, TokenBlocklistService tokenBlocklistService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest req) {
        // TODO: add email address
        userService.register(req.username(), req.email(), req.password());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        var auth = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        authManager.authenticate(auth);
        String token = jwtService.generateToken(req.username());
        return new TokenResponse(token);
    }

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
}
