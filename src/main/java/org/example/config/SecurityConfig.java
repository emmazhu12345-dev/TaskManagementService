package org.example.config;

import org.example.auth.CustomUserPrincipal;
import org.example.auth.JwtAuthenticationFilter;
import org.example.repository.UserRepository;
import org.example.service.UserLookupService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/** Security configuration: JWT + username/password auth. */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Bridge repository user to Spring Security's UserDetails. */
    @Bean
    public UserDetailsService userDetailsService(UserLookupService userLookupService) {
        return username -> {
            var user = userLookupService.getByUsernameOrThrow(username);

            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
            return new CustomUserPrincipal(
                    user.getId(),
                    user.getUsername(),
                    user.getPasswordHash(),
                    authorities,
                    user.isActive()
            );
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationFilter jwtFilter, AuthenticationProvider authProvider, Environment env) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // <-- stateless
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(reg -> {
                    reg.requestMatchers("/api/auth/**", "/h2-console/**").permitAll();
                    reg.requestMatchers("/api/auth/logout").authenticated();
                    // Make debug endpoints public in dev only:
                    if (env.matchesProfiles("dev")) {
                        reg.requestMatchers("/api/debug/**", "/api/notes/**").permitAll();
                    }
                    reg.anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
